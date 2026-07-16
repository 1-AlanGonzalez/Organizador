import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const PASSWORD = __ENV.K6_PASSWORD || "loadtest123";
const VUS = Number(__ENV.VUS || 10);
const DURATION = __ENV.DURATION || "1m";

let sesionIniciada = false;
let numeroGimnasio;

export const options = {
    noCookiesReset: true,
    scenarios: {
        carga_inicial: {
            executor: "constant-vus",
            vus: VUS,
            duration: DURATION,
            gracefulStop: "10s",
        },
    },
    thresholds: {
        checks: ["rate>0.99"],
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<1500"],
    },
};

function iniciarSesion() {
    numeroGimnasio = (__VU % 2) + 1;
    const username = `gym_load_${numeroGimnasio}`;
    const loginPage = http.get(`${BASE_URL}/login`, {
        tags: { name: "GET /login" },
    });
    const csrf = loginPage.html().find('input[name="_csrf"]').attr("value");

    const login = http.post(
        `${BASE_URL}/login`,
        { username, password: PASSWORD, _csrf: csrf },
        { redirects: 0, tags: { name: "POST /login" } }
    );

    const correcto = check(login, {
        "login concurrente aceptado": response =>
            response.status === 302 &&
            (response.headers.Location || "").includes("/dashboard"),
    });
    sesionIniciada = correcto;
}

function verificarPagina(ruta, textoEsperado, nombre) {
    const respuesta = http.get(`${BASE_URL}${ruta}`, {
        redirects: 0,
        tags: { name: `GET ${nombre}` },
    });
    check(respuesta, {
        [`${nombre} responde 200`]: response => response.status === 200,
        [`${nombre} no redirige al login`]: response =>
            response.status !== 302 || !(response.headers.Location || "").includes("/login"),
        [`${nombre} muestra su contenido`]: response =>
            response.status === 200 && response.body.includes(textoEsperado),
    });
    return respuesta;
}

export default function () {
    if (!sesionIniciada) iniciarSesion();
    if (!sesionIniciada) return;

    const escenario = __ITER % 6;
    if (escenario === 0) {
        verificarPagina("/dashboard", "Resumen general del gimnasio", "dashboard");
    } else if (escenario === 1) {
        const clientes = verificarPagina("/clientes?page=0", "Directorio de Clientes", "clientes");
        const otroGimnasio = numeroGimnasio === 1 ? "Carga2" : "Carga1";
        check(clientes, {
            "clientes no mezcla gimnasios": response => !response.body.includes(otroGimnasio),
        });
    } else if (escenario === 2) {
        verificarPagina("/actividades?page=0", "Gestión de clases", "actividades");
    } else if (escenario === 3) {
        verificarPagina("/instructores?page=0", "Gestión del personal", "instructores");
    } else if (escenario === 4) {
        verificarPagina("/ingresos?page=0", "Tablero Financiero", "ingresos");
    } else {
        verificarPagina("/asistencias?page=0", "Gestión de Asistencias", "asistencias");
    }

    sleep(1);
}

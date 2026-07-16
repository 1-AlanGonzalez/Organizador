import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const USERNAME = __ENV.K6_USERNAME || "gym_load_1";
const PASSWORD = __ENV.K6_PASSWORD || "loadtest123";

export const options = {
    vus: 1,
    iterations: 1,
    thresholds: {
        http_req_failed: ["rate==0"],
        http_req_duration: ["p(95)<1500"],
        checks: ["rate==1"],
    },
};

export default function () {
    const loginPage = http.get(`${BASE_URL}/login`, {
        tags: { name: "GET /login" },
    });

    const csrf = loginPage.html().find('input[name="_csrf"]').attr("value");
    check(loginPage, {
        "login responde 200": response => response.status === 200,
        "login contiene CSRF": () => Boolean(csrf),
    });

    const login = http.post(
        `${BASE_URL}/login`,
        {
            username: USERNAME,
            password: PASSWORD,
            _csrf: csrf,
        },
        {
            redirects: 0,
            tags: { name: "POST /login" },
        }
    );

    check(login, {
        "credenciales aceptadas": response => response.status === 302,
        "redirige al dashboard": response =>
            (response.headers.Location || "").includes("/dashboard"),
    });

    const dashboard = http.get(`${BASE_URL}/dashboard`, {
        tags: { name: "GET /dashboard" },
    });
    check(dashboard, {
        "dashboard responde 200": response => response.status === 200,
        "dashboard no vuelve al login": response => !response.url.includes("/login"),
        "dashboard muestra su contenido": response =>
            response.body.includes("Resumen general del gimnasio"),
    });

    sleep(1);

    const clientes = http.get(`${BASE_URL}/clientes?page=0`, {
        tags: { name: "GET /clientes" },
    });
    check(clientes, {
        "clientes responde 200": response => response.status === 200,
        "clientes pertenece al gimnasio autenticado": response =>
            response.body.includes("Directorio de Clientes"),
    });
}

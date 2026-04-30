const API_BASE_URL = "http://localhost:8080";

function getToken() {
    return localStorage.getItem("token") || "";
}

function getRole() {
    return localStorage.getItem("role") || "";
}

function getAuthHeaders(extraHeaders = {}) {
    const headers = {
        ...extraHeaders
    };

    const token = getToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, options);

    let result;
    try {
        result = await response.json();
    } catch (error) {
        throw new Error("Server returned a non-JSON response.");
    }

    if (!response.ok || result.code !== 200) {
        throw new Error(result.message || "Request failed.");
    }

    return result.data;
}

async function apiGet(path) {
    return apiRequest(path, {
        method: "GET",
        headers: getAuthHeaders()
    });
}

async function apiPost(path, body) {
    return apiRequest(path, {
        method: "POST",
        headers: getAuthHeaders({
            "Content-Type": "application/json"
        }),
        body: JSON.stringify(body)
    });
}

async function apiPut(path, body) {
    return apiRequest(path, {
        method: "PUT",
        headers: getAuthHeaders({
            "Content-Type": "application/json"
        }),
        body: JSON.stringify(body)
    });
}

async function apiPutForm(path, params = {}) {
    const search = new URLSearchParams(params).toString();
    const fullPath = search ? `${path}?${search}` : path;

    return apiRequest(fullPath, {
        method: "PUT",
        headers: getAuthHeaders()
    });
}

function saveAuth(data) {
    localStorage.setItem("token", data.token || "");
    localStorage.setItem("userId", String(data.userId || ""));
    localStorage.setItem("role", data.role || "");
    localStorage.setItem("fullName", data.fullName || "");
}

function clearAuth() {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    localStorage.removeItem("fullName");
}

function redirectByRole(role) {
    if (role === "admin") {
        window.location.href = "/admin-dashboard.html";
        return;
    }

    if (role === "employer") {
        window.location.href = "/employer-dashboard.html";
        return;
    }

    if (role === "student") {
        window.location.href = "/jobseeker-dashboard.html";
        return;
    }

    window.location.href = "/login.html";
}

function requireRole(requiredRole) {
    const token = getToken();
    const role = getRole();

    if (!token || role !== requiredRole) {
        clearAuth();
        if (requiredRole === "admin") {
            window.location.href = "/login-admin.html";
            return false;
        }
        window.location.href = "/login.html";
        return false;
    }

    return true;
}

function showText(elementId, text, isError = false) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.textContent = text || "";
    el.style.color = isError ? "#d93025" : "#2b7a0b";
}
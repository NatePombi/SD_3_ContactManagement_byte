const configuredBaseUrl =
    import.meta.env.VITE_API_BASE_URL || "";

export const API_BASE_URL =
    configuredBaseUrl.replace(/\/+$/, "");

export function apiUrl(path) {
    if (!path.startsWith("/")) {
        throw new Error("API path must start with /");
    }

    return `${API_BASE_URL}${path}`;
}
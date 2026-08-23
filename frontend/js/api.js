

const API_BASE_URL = 'http://localhost:8081/api';

async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;

    const defaultHeaders = {
        'Content-Type': 'application/json'
    };

    const currentUserStr = localStorage.getItem('currentUser');
    if (currentUserStr) {
        try {
            const currentUser = JSON.parse(currentUserStr);
            if (currentUser && currentUser.token) {
                defaultHeaders['Authorization'] = `Bearer ${currentUser.token}`;
            }
        } catch (e) {
            console.error('Error parsing currentUser for Auth header:', e);
        }
    }

    const config = {
        method: options.method || 'GET',
        headers: {
            ...defaultHeaders,
            ...options.headers
        }
    };

    if (options.body) {
        config.body = typeof options.body === 'string' ? options.body : JSON.stringify(options.body);
    }

    try {
        const response = await fetch(url, config);

        
        if (response.status === 204) {
            return null;
        }

        const contentType = response.headers.get('content-type');
        let data = null;
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {

            data = { message: await response.text() };
        }

        if (!response.ok) {
            const error = new Error(data.message || 'API Request Failed');
            error.status = response.status;
            error.details = data;
            throw error;
        }

        return data;
    } catch (error) {
        console.error(`API Error on endpoint ${endpoint}:`, error);
        throw error;
    }
}

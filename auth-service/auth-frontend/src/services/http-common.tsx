import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8091',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: false,
});

export default instance;

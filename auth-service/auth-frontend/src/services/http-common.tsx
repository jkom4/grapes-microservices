import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://89.168.47.217:8090/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: false,
});

export default instance;

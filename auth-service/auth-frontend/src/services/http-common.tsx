import axios from 'axios';

const instance = axios.create({
    baseURL: 'https://api.grapesmasi.me/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: false,
});

export default instance;

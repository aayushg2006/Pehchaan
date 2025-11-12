import axios from 'axios';

// Create a new Axios instance
const api = axios.create({
  // This is the base URL of our Spring Boot backend
  baseURL: 'http://localhost:8080',
});

/*
  This is a 'request interceptor'.
  It's a powerful feature that will automatically add our JWT token
  to the header of *every single request* we send, but only
  if we have a token.
*/
api.interceptors.request.use(
  (config) => {
    // Get the token from local storage (we will save it there on login)
    const token = localStorage.getItem('token');
    
    if (token) {
      // If the token exists, add it to the Authorization header
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    // Handle any request errors
    return Promise.reject(error);
  }
);

export default api;
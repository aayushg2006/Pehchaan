import api from './api'; // Import our pre-configured Axios instance

// Function to call the registration endpoint
const register = (phone, password, role) => {
  return api.post('/api/auth/register', {
    phone,
    password,
    role,
  });
};

// Function to call the login endpoint
const login = (phone, password) => {
  return api.post('/api/auth/login', {
    phone,
    password,
  });
};

// Simple function to log the user out (by removing the token)
const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  // We'll also redirect the user to the login page here
};

// Export all the functions so our components can use them
const authService = {
  register,
  login,
  logout,
};

export default authService;
import React, { createContext, useState, useEffect, useContext } from 'react';
import authService from '../services/authService.js';
import profileService from '../services/profileService.js'; // <-- IMPORT PROFILE SERVICE
import api from '../services/api.js'; // <-- IMPORT API FOR HEADER

// 1. Create the context
const AuthContext = createContext();

// Helper hook for components to easily use the context
export const useAuth = () => {
  return useContext(AuthContext);
};

// 2. Create the provider component
const AuthProvider = ({ children }) => {
  // We'll store the full user object now (or null)
  const [user, setUser] = useState(null);
  // Add a loading state to prevent screen flicker
  const [loading, setLoading] = useState(true);

  // This effect runs ONCE when the app loads
  useEffect(() => {
    const loadUserFromToken = async () => {
      const token = localStorage.getItem('token');
      if (token) {
        try {
          // Set the token on our API header
          api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
          
          // Fetch the user's profile
          const response = await profileService.getMyProfile();
          setUser(response.data); // Set the full user object
        } catch (error) {
          // Token is invalid or expired
          authService.logout();
        }
      }
      setLoading(false);
    };

    loadUserFromToken();
  }, []);

  // --- Core Functions ---

  const login = async (phone, password) => {
    const response = await authService.login(phone, password);
    const { token } = response.data;
    
    localStorage.setItem('token', token);
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;

    // After login, fetch the full profile
    const profileResponse = await profileService.getMyProfile();
    setUser(profileResponse.data);
  };

  const register = async (phone, password, role) => {
    const response = await authService.register(phone, password, role);
    const { token } = response.data;

    localStorage.setItem('token', token);
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;

    // After register, fetch the (incomplete) profile
    const profileResponse = await profileService.getMyProfile();
    setUser(profileResponse.data);
  };

  const logout = () => {
    authService.logout(); // Clears localStorage
    delete api.defaults.headers.common['Authorization']; // Clear api header
    setUser(null);
  };

  // We pass down the user, the functions, and the loading state
  const value = {
    user,
    loading,
    login,
    register,
    logout,
    // Add a function to allow components to update the user state (e.g., after profile update)
    setUser 
  };

  // Show nothing while we're loading the user's status
  if (loading) {
    return <div>Loading...</div>; // Or a fancy spinner component
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// 3. Export the provider
export { AuthProvider };
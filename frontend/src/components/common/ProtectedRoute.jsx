import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext'; // ✅ CORRECTED: This file already used it

const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  let location = useLocation();

  if (!user) {
    // 1. If user is not logged in, redirect to login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (!user.firstName) {
    // 2. If user IS logged in but has no first name, force profile completion
    return <Navigate to="/complete-profile" state={{ from: location }} replace />;
  }

  // 3. If user is logged in AND has a first name, show the page
  return children;
};

export default ProtectedRoute;
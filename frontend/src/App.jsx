import React from 'react';
import { Routes, Route, Navigate, useLocation } from 'react-router-dom';
import './App.css';
import { useAuth } from './context/AuthContext';

import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import Dashboard from './pages/Dashboard.jsx';
import CompleteProfile from './pages/CompleteProfile.jsx';

/**
 * ProtectedRoute:
 * - Not logged in? -> Go to /login
 * - Logged in BUT profile incomplete? -> Go to /complete-profile
 * - Logged in AND profile complete? -> Show the page
 */
function ProtectedRoute({ children }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user) {
    // 1. If user is not logged in, redirect to login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 2. If user IS logged in but has no first name, force profile completion
  //    (We also check they are not already ON the complete-profile page)
  if (!user.firstName && location.pathname !== '/complete-profile') {
    return <Navigate to="/complete-profile" state={{ from: location }} replace />;
  }
  
  // 3. If user IS logged in and trying to access /complete-profile but *has* a first name,
  //    send them to the dashboard.
  if (user.firstName && location.pathname === '/complete-profile') {
      return <Navigate to="/" state={{ from: location }} replace />;
  }

  // 4. If all checks pass, show the requested page
  return children;
}

/**
 * AuthRoute (or GuestRoute):
 * - Logged in? -> Go to / (dashboard)
 * - Not logged in? -> Show the login/register page
 */
function AuthRoute({ children }) {
  const { user } = useAuth();
  
  if (user) {
    // If user is already logged in, don't show login page
    return <Navigate to="/" replace />;
  }

  return children;
}

function App() {
  const { loading } = useAuth();

  // Show a blank loading screen while AuthContext is 
  // checking if a token exists in localStorage
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontFamily: 'var(--font-heading)' }}>
        Loading Pehchaan...
      </div>
    );
  }

  return (
    <div className="app-container">
      <Routes>
        {/* --- Protected Routes --- */}
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/complete-profile"
          element={
            <ProtectedRoute>
              <CompleteProfile />
            </ProtectedRoute>
          }
        />

        {/* --- Guest-Only Routes --- */}
        <Route
          path="/login"
          element={
            <AuthRoute>
              <LoginPage />
            </AuthRoute>
          }
        />
        <Route
          path="/register"
          element={
            <AuthRoute>
              <RegisterPage />
            </AuthRoute>
          }
        />
      </Routes>
    </div>
  );
}

export default App;
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthForm.css'; // Use our new professional styles

const LoginPage = () => {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); 

    try {
      await login(phone, password);
      // Navigation now happens automatically in App.jsx
      // but we can keep this as a fallback.
      navigate('/'); 
    } catch (err) {
      setError('Login failed. Please check your phone and password.');
      console.error('Login error:', err);
    }
  };

  return (
    <div className="auth-layout">
      {/* 1. The Left-Side Graphic */}
      <div className="auth-graphic">
        <div className="auth-graphic-logo">Pehchaan</div>
        <p className="auth-graphic-subtitle">
          Your digital identity for a secure future. Track your work, get paid on time.
        </p>
      </div>

      {/* 2. The Right-Side Form Content */}
      <div className="auth-content">
        <form onSubmit={handleSubmit} className="auth-form">
          <h2>Login to Pehchaan</h2>
          {error && <p className="error-message">{error}</p>}
          
          <div className="form-group">
            <label htmlFor="phone">Phone Number</label>
            <input
              type="text"
              id="phone"
              autoComplete="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          
          <button type="submit" className="auth-button">Login</button>
          
          <p className="auth-switch">
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
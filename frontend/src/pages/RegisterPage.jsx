import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthForm.css'; // Reuse our new professional styles

const RegisterPage = () => {
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('ROLE_LABOR'); 
  const [error, setError] = useState('');
  const { register } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    try {
      await register(phone, password, role);
      // Navigation will be handled by App.jsx,
      // which will see we are logged in but have no first name,
      // and send us to /complete-profile
      navigate('/'); 
    } catch (err) {
      setError('Registration failed. Please try again.');
      console.error('Registration error:', err);
    }
  };

  return (
    <div className="auth-layout">
      {/* 1. The Left-Side Graphic */}
      <div className="auth-graphic">
        <div className="auth-graphic-logo">Pehchaan</div>
        <p className="auth-graphic-subtitle">
          Join India's largest digital workforce. Your work, your identity, your payments—all in one place.
        </p>
      </div>

      {/* 2. The Right-Side Form Content */}
      <div className="auth-content">
        <form onSubmit={handleSubmit} className="auth-form">
          <h2>Create your Pehchaan</h2>
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
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="role">I am a:</label>
            <select 
              id="role" 
              value={role} 
              onChange={(e) => setRole(e.target.value)}
            >
              <option value="ROLE_LABOR">Laborer / Worker</option>
              <option value="ROLE_CONTRACTOR">Contractor</option>
              <option value="ROLE_CONSUMER">Consumer</option>
            </select>
          </div>
          
          <button type="submit" className="auth-button">Register</button>
          
          <p className="auth-switch">
            Already have an account? <Link to="/login">Login here</Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default RegisterPage;
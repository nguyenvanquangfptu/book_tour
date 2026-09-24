import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaUser, FaEnvelope, FaLock, FaUserPlus, FaEye, FaEyeSlash } from 'react-icons/fa';
import { GoogleLogin } from '@react-oauth/google';
import { AuthService } from '../services/AuthService';
import { useAuthStore } from '../store/useAuthStore';
import { useTranslation } from 'react-i18next';
import '../styles/auth.css';

const Register: React.FC = () => {
  const { t } = useTranslation();
  const { login } = useAuthStore();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    fullName: '',
    username: '',
    email: '',
    password: '',
    confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // States to prevent autofill
  const [focusStates, setFocusStates] = useState({
    fullName: false,
    username: false,
    email: false,
    password: false,
    confirmPassword: false
  });

  const handleFocus = (field: string) => {
    setFocusStates(prev => ({ ...prev, [field]: true }));
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    if (formData.password !== formData.confirmPassword) {
      setError(t('auth.passwordMismatch'));
      setLoading(false);
      return;
    }

    try {
      const registerData = {
        fullName: formData.fullName,
        username: formData.username,
        email: formData.email,
        password: formData.password
      };
      
      const data = await AuthService.register(registerData);
      if (data) {
        setSuccess(true);
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.registerFail'));
      console.error('Register error', err);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSuccess = async (credentialResponse: any) => {
    setLoading(true);
    setError('');
    try {
      const { credential } = credentialResponse;
      const data = await AuthService.googleLogin(credential);
      if (data && data.token) {
        login({
          id: data.userId,
          username: data.username,
          role: data.role,
          fullName: data.fullName,
          email: data.email,
          avatarUrl: data.avatar
        }, data.token);
        navigate('/');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.googleLoginFail'));
      console.error('Google login error', err);
    } finally {
      setLoading(false);
    }
  };

  const isPasswordShort = formData.password.length > 0 && formData.password.length < 8;
  const isPasswordMismatch = formData.confirmPassword.length > 0 && formData.password !== formData.confirmPassword;
  const isFormInvalid = isPasswordShort || isPasswordMismatch;

  return (
    <div className="auth-page">
      <div className="auth-overlay"></div>
      <div className="auth-container animate-fade-in">
        <div className="auth-card glass-card">
          <div className="auth-header">
            <h2>{t('auth.registerTitle')}</h2>
            <p>{t('auth.registerSubtitle')}</p>
          </div>

          {error && <div className="auth-error">{error}</div>}
          {success && <div className="auth-success">{t('auth.registerSuccess')}</div>}

          <form onSubmit={handleSubmit} className="auth-form" autoComplete="off">
            <div className="input-group">
              <label className="input-label">{t('auth.fullNameLabel')}</label>
              <div className="auth-input-wrapper">
                <FaUser className="auth-icon" />
                <input 
                  type="text" 
                  name="fullName"
                  className="input-field" 
                  placeholder={t('auth.fullNamePlaceholder')}
                  value={formData.fullName}
                  onChange={handleInputChange}
                  autoComplete="name"
                  readOnly={!focusStates.fullName}
                  onFocus={() => handleFocus('fullName')}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">{t('auth.usernameLabel')}</label>
              <div className="auth-input-wrapper">
                <FaUser className="auth-icon" />
                <input 
                  type="text" 
                  name="username"
                  className="input-field" 
                  placeholder={t('auth.usernamePlaceholder')}
                  value={formData.username}
                  onChange={handleInputChange}
                  autoComplete="username"
                  readOnly={!focusStates.username}
                  onFocus={() => handleFocus('username')}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">{t('auth.emailLabel')}</label>
              <div className="auth-input-wrapper">
                <FaEnvelope className="auth-icon" />
                <input 
                  type="email" 
                  name="email"
                  className="input-field" 
                  placeholder={t('auth.emailPlaceholder')}
                  value={formData.email}
                  onChange={handleInputChange}
                  autoComplete="email"
                  readOnly={!focusStates.email}
                  onFocus={() => handleFocus('email')}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">{t('auth.passwordLabel')}</label>
              <div className="auth-input-wrapper">
                <FaLock className="auth-icon" />
                <input 
                  type={showPassword ? "text" : "password"} 
                  name="password"
                  className="input-field" 
                  placeholder={t('auth.passwordPlaceholder')}
                  value={formData.password}
                  onChange={handleInputChange}
                  readOnly={!focusStates.password}
                  onFocus={() => handleFocus('password')}
                  required 
                />
                <button 
                  type="button" 
                  onClick={() => setShowPassword(!showPassword)}
                  style={{ position: 'absolute', right: '15px', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                  tabIndex={-1}
                >
                  {showPassword ? <FaEyeSlash /> : <FaEye />}
                </button>
              </div>
              {isPasswordShort && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>{t('auth.passwordShort')}</span>}
            </div>

            <div className="input-group">
              <label className="input-label">{t('auth.confirmPasswordLabel')}</label>
              <div className="auth-input-wrapper">
                <FaLock className="auth-icon" />
                <input 
                  type={showConfirmPassword ? "text" : "password"} 
                  name="confirmPassword"
                  className="input-field" 
                  placeholder={t('auth.confirmPasswordPlaceholder')}
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  autoComplete="new-password"
                  readOnly={!focusStates.confirmPassword}
                  onFocus={() => handleFocus('confirmPassword')}
                  required 
                />
                <button 
                  type="button" 
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  style={{ position: 'absolute', right: '15px', background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                  tabIndex={-1}
                >
                  {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                </button>
              </div>
              {isPasswordMismatch && <span style={{color: '#e74c3c', fontSize: '13px', marginTop: '4px', display: 'block', textAlign: 'left', fontWeight: '500'}}>{t('auth.passwordMismatch')}</span>}
            </div>

            <button type="submit" className="btn btn-primary auth-btn" disabled={loading || success || isFormInvalid}>
              {loading ? t('auth.processing') : (
                <>
                  <FaUserPlus /> {t('auth.registerBtn')}
                </>
              )}
            </button>
          </form>

          <div className="auth-divider">
            <span>{t('auth.orRegisterWith')}</span>
          </div>

          <div className="auth-social" style={{ display: 'flex', justifyContent: 'center', marginTop: '16px' }}>
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => {
                setError(t('auth.googleLoginFail'));
              }}
              useOneTap
            />
          </div>

          <div className="auth-footer" style={{ marginTop: '24px' }}>
            <p>{t('auth.hasAccount')} <Link to="/login" className="auth-link">{t('auth.loginNow')}</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;

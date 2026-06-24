import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaEnvelope, FaLock, FaSignInAlt } from 'react-icons/fa';
import { GoogleLogin } from '@react-oauth/google';
import { AuthService } from '../services/AuthService';
import { useTranslation } from 'react-i18next';
import '../styles/auth.css';

const Login: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [isUsernameFocused, setIsUsernameFocused] = useState(false);
  const [isPasswordFocused, setIsPasswordFocused] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const data = await AuthService.login(formData);
      if (data && data.token) {
        // Lưu token và user info
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
          id: data.userId,
          username: data.username,
          role: data.role
        }));
        
        // Chuyển hướng về trang chủ và tải lại trang để navbar nhận diện trạng thái login
        window.location.href = '/';
      }
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.loginFail'));
      console.error('Login error', err);
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
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify({
          id: data.userId,
          username: data.username,
          role: data.role
        }));
        window.location.href = '/';
      }
    } catch (err: any) {
      setError(err.response?.data?.message || t('auth.googleLoginFail'));
      console.error('Google login error', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-overlay"></div>
      <div className="auth-container animate-fade-in">
        <div className="auth-card glass-card">
          <div className="auth-header">
            <h2>{t('auth.loginTitle')}</h2>
            <p>{t('auth.loginSubtitle')}</p>
          </div>

          {error && <div className="auth-error">{error}</div>}

          <form onSubmit={handleSubmit} className="auth-form" autoComplete="off">
            <div className="input-group">
              <label className="input-label">{t('auth.usernameLabel')}</label>
              <div className="auth-input-wrapper">
                <FaEnvelope className="auth-icon" />
                <input 
                  type="text" 
                  name="username"
                  className="input-field" 
                  placeholder={t('auth.usernamePlaceholder')}
                  value={formData.username}
                  onChange={handleInputChange}
                  autoComplete="username"
                  readOnly={!isUsernameFocused}
                  onFocus={() => setIsUsernameFocused(true)}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <div className="label-with-link">
                <label className="input-label">{t('auth.passwordLabel')}</label>
                <Link to="/forgot-password" className="auth-link">{t('auth.forgotPasswordLink')}</Link>
              </div>
              <div className="auth-input-wrapper">
                <FaLock className="auth-icon" />
                <input 
                  type="password" 
                  name="password"
                  className="input-field" 
                  placeholder={t('auth.passwordPlaceholder')}
                  value={formData.password}
                  onChange={handleInputChange}
                  autoComplete="current-password"
                  readOnly={!isPasswordFocused}
                  onFocus={() => setIsPasswordFocused(true)}
                  required 
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary auth-btn" disabled={loading}>
              {loading ? t('auth.processing') : (
                <>
                  <FaSignInAlt /> {t('auth.loginBtn')}
                </>
              )}
            </button>
          </form>

          <div className="auth-divider">
            <span>{t('auth.orLoginWith')}</span>
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
            <p>{t('auth.noAccount')} <Link to="/register" className="auth-link">{t('auth.registerNow')}</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;

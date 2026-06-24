import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaSuitcase, FaShoppingCart, FaSearch, FaUser, FaHistory, FaHeart, FaSignOutAlt } from 'react-icons/fa';
import { useCartStore } from '../store/useCartStore';
import { useAuthStore } from '../store/useAuthStore';
import { useTranslation } from 'react-i18next';
import '../styles/navbar.css';

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [scrolled, setScrolled] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [langDropdownOpen, setLangDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const langDropdownRef = useRef<HTMLDivElement>(null);
  
  const { t, i18n } = useTranslation();
  
  const { getCartCount } = useCartStore();
  const { user, isAuthenticated, logout } = useAuthStore();
  
  // These pages have a hero image at the top, so navbar can be transparent initially
  const transparentNavPaths = ['/', '/tours', '/about', '/contact'];
  const isTransparentNavPage = transparentNavPaths.includes(location.pathname);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50);
    };
    window.addEventListener('scroll', handleScroll);
    
    // Check initial scroll position
    handleScroll();
    
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);
  
  // Determine if navbar should be solid based on scroll or page type
  const isSolidNav = scrolled || !isTransparentNavPage;

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
      if (langDropdownRef.current && !langDropdownRef.current.contains(event.target as Node)) {
        setLangDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setDropdownOpen(false);
    navigate('/login');
  };

  const getInitials = (name: string) => {
    if (!name) return 'U';
    return name.charAt(0).toUpperCase();
  };

  return (
    <nav className={`navbar ${isSolidNav ? 'navbar-scrolled' : ''}`}>
      <div className="container navbar-container">
        {/* Brand */}
        <Link to="/" className="navbar-brand">
          <FaSuitcase className="brand-icon" />
          <span>BookingTour</span>
        </Link>

        {/* Menu Links */}
        <div className="navbar-menu">
          <Link to="/tours" className="nav-link">{t('navbar.tours')}</Link>
          <Link to="/about" className="nav-link">{t('navbar.about')}</Link>
          <Link to="/contact" className="nav-link">{t('navbar.contact')}</Link>
        </div>

        {/* Search & Auth */}
        <div className="navbar-auth">
          {/* Search Bar */}
          <div className="navbar-search">
            <FaSearch className="navbar-search-icon" />
            <input 
              type="text" 
              placeholder={t('navbar.searchPlaceholder')} 
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  navigate(`/tours?keyword=${encodeURIComponent(e.currentTarget.value)}`);
                }
              }}
            />
          </div>

          {isAuthenticated && user && (
            <Link to="/cart" className="cart-icon-wrapper">
              <FaShoppingCart />
              {getCartCount() > 0 && (
                <span className="badge" style={{ position: 'absolute', top: '-10px', right: '-12px', background: '#f97316', color: '#fff', fontSize: '0.75rem', padding: '2px 6px', borderRadius: '50%', minWidth: '18px', textAlign: 'center' }}>
                  {getCartCount()}
                </span>
              )}
            </Link>
          )}

          {isAuthenticated && user ? (
            <div className="avatar-dropdown" ref={dropdownRef}>
              <div className="avatar-btn" onClick={() => setDropdownOpen(!dropdownOpen)}>
                {/* Random avatar from ui-avatars or unsplash. For now, use initials */}
                {user.avatarUrl ? (
                  <img src={user.avatarUrl} alt="Avatar" />
                ) : (
                  getInitials(user.fullName || user.username || 'User')
                )}
              </div>
              
              <div className={`dropdown-menu ${dropdownOpen ? 'active' : ''}`}>
                {user.role === 'ADMIN' && (
                  <>
                    <Link to="/admin/tours" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                      <span style={{ color: 'var(--danger)' }}>{t('navbar.dashboard')}</span>
                    </Link>
                    <div className="dropdown-divider"></div>
                  </>
                )}
                
                <Link to="/profile" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                  <FaUser /> {t('navbar.profile')}
                </Link>
                <div className="dropdown-item" onClick={() => { navigate('/profile'); setDropdownOpen(false); }}>
                  <FaHistory /> {t('navbar.myBooking')}
                </div>
                <div className="dropdown-item" onClick={() => { navigate('/tours'); setDropdownOpen(false); }}>
                  <FaHeart /> {t('navbar.wishlist')}
                </div>
                <div className="dropdown-divider"></div>
                <div className="dropdown-item" onClick={handleLogout} style={{ color: 'var(--danger)' }}>
                  <FaSignOutAlt /> {t('navbar.logout')}
                </div>
              </div>
            </div>
          ) : (
            <>
              <Link to="/login" className="nav-link" style={{color: isSolidNav ? 'var(--text-primary)' : 'white'}}>{t('navbar.login')}</Link>
              <Link to="/register" className="btn btn-primary" style={{ padding: '8px 20px' }}>{t('navbar.signup')}</Link>
            </>
          )}

          {/* Language Switcher */}
          <div className="avatar-dropdown" ref={langDropdownRef} style={{ marginLeft: '10px' }}>
            <div className="avatar-btn" onClick={() => setLangDropdownOpen(!langDropdownOpen)} style={{ background: 'transparent', fontSize: '20px', padding: '0 5px' }}>
              {i18n.language === 'en' ? '🇺🇸' : '🇻🇳'}
            </div>
            <div className={`dropdown-menu ${langDropdownOpen ? 'active' : ''}`} style={{ minWidth: '120px' }}>
              <div className="dropdown-item" onClick={() => { i18n.changeLanguage('vi'); setLangDropdownOpen(false); }}>
                🇻🇳 Tiếng Việt
              </div>
              <div className="dropdown-item" onClick={() => { i18n.changeLanguage('en'); setLangDropdownOpen(false); }}>
                🇺🇸 English
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

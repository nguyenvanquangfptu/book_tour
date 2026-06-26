import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaSearch, FaUser, FaHistory, FaHeart, FaSignOutAlt, FaShoppingCart } from 'react-icons/fa';
import { useAuthStore } from '../store/useAuthStore';
import { useCartStore } from '../store/useCartStore';
import { useTourOptions } from '../hooks/useTourOptions';
import { useTranslation } from 'react-i18next';
import '../styles/navbar.css';

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [scrolled, setScrolled] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [langDropdownOpen, setLangDropdownOpen] = useState(false);
  const [destDropdownOpen, setDestDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const langDropdownRef = useRef<HTMLDivElement>(null);
  const destDropdownRef = useRef<HTMLDivElement>(null);
  
  const { t, i18n } = useTranslation();
  
  const { user, isAuthenticated, logout } = useAuthStore();
  const { data: optionsData } = useTourOptions();
  const allDestinations = optionsData?.destinations || [];

  // Compact Search States
  const [destInputValue, setDestInputValue] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [guests, setGuests] = useState('2');
  
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
  
  // Hide compact search on specific pages
  const hideCompactSearchPaths = ['/profile', '/login', '/register', '/admin', '/cart', '/checkout'];
  const shouldShowCompactSearch = isSolidNav && !hideCompactSearchPaths.some(p => location.pathname.startsWith(p));
  
  const cart = useCartStore(state => state.cart);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
      if (langDropdownRef.current && !langDropdownRef.current.contains(event.target as Node)) {
        setLangDropdownOpen(false);
      }
      if (destDropdownRef.current && !destDropdownRef.current.contains(event.target as Node)) {
        setDestDropdownOpen(false);
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

  const handleSearchClick = () => {
    let query = `/tours?dest=${encodeURIComponent(destInputValue)}`;
    
    if (checkIn && checkOut) {
      const diffTime = new Date(checkOut).getTime() - new Date(checkIn).getTime();
      if (diffTime >= 0) {
        query += `&checkIn=${checkIn}&checkOut=${checkOut}`;
      }
    }
    
    if (guests) {
      query += `&guests=${guests}`;
    }
    
    navigate(query);
  };

  return (
    <nav className={`navbar ${isSolidNav ? 'navbar-scrolled' : ''}`}>
      <div className="container navbar-container">
        {/* Brand */}
        <Link to="/" className="navbar-brand">
          <span style={{ color: isSolidNav ? '#0f172a' : '#fff', fontWeight: 800, fontSize: '1.6rem' }}>BookTour</span>
        </Link>

        {/* Compact Search Bar (visible when scrolled) */}
        <div className={`navbar-compact-search ${shouldShowCompactSearch ? 'visible' : ''}`}>
          <div className="compact-search-item dest-dropdown-wrapper" ref={destDropdownRef}>
            <span className="compact-label">{t('searchBar.where')}</span>
            <input 
              type="text" 
              placeholder={t('searchBar.searchDestinations')}
              value={destInputValue}
              onChange={(e) => setDestInputValue(e.target.value)}
              onFocus={(e) => {
                e.target.select();
                setDestDropdownOpen(true);
              }}
              style={{ border: 'none', background: 'transparent', outline: 'none', width: '130px', fontSize: '0.85rem', color: '#0ea5e9', padding: 0 }}
            />
            
            <div className={`dest-dropdown-menu ${destDropdownOpen ? 'active' : ''}`}>
              <div className="dest-dropdown-header">Destinations</div>
              <div className="dest-dropdown-list" style={{ maxHeight: '250px', overflowY: 'auto' }}>
                {allDestinations
                  .filter((d: string) => destInputValue === '' || allDestinations.includes(destInputValue) || d.toLowerCase().includes(destInputValue.toLowerCase()))
                  .map((dest: string) => (
                  <div key={dest} className="dest-dropdown-item" onClick={(e) => { e.stopPropagation(); setDestInputValue(dest); setDestDropdownOpen(false); }}>
                    <span className="dest-icon">📍</span> {dest}
                  </div>
                ))}
                {allDestinations.length === 0 && (
                  <div className="dest-dropdown-item" style={{ color: '#94a3b8' }}>No destinations</div>
                )}
              </div>
            </div>
          </div>
          <div className="compact-divider"></div>
          <div className="compact-search-item">
            <span className="compact-label">{t('searchBar.departure')}</span>
            <input type="date" min={new Date().toISOString().split('T')[0]} value={checkIn} onChange={(e) => setCheckIn(e.target.value)} style={{ border: 'none', background: 'transparent', outline: 'none', fontSize: '0.85rem', padding: 0 }} />
          </div>
          <div className="compact-divider"></div>
          <div className="compact-search-item">
            <span className="compact-label">{t('searchBar.return')}</span>
            <input type="date" min={checkIn || new Date().toISOString().split('T')[0]} value={checkOut} onChange={(e) => setCheckOut(e.target.value)} style={{ border: 'none', background: 'transparent', outline: 'none', fontSize: '0.85rem', padding: 0 }} />
          </div>
          <div className="compact-divider"></div>
          <div className="compact-search-item" style={{ minWidth: '60px' }}>
            <span className="compact-label">{t('searchBar.guests')}</span>
            <input type="number" min="1" value={guests} onChange={(e) => setGuests(e.target.value)} style={{ border: 'none', background: 'transparent', outline: 'none', fontSize: '0.85rem', padding: 0, width: '40px' }} />
          </div>
          <button className="compact-search-btn" onClick={handleSearchClick}>
            <FaSearch />
          </button>
        </div>

        {/* Search & Auth */}
        <div className="navbar-auth" style={{ display: 'flex', alignItems: 'center' }}>
          {/* Cart Icon */}
          <Link to="/cart" className="cart-icon-wrapper" style={{ marginRight: '15px', textDecoration: 'none', display: 'flex', alignItems: 'center', color: isSolidNav ? '#0f172a' : 'white' }}>
            <FaShoppingCart style={{ fontSize: '1.2rem' }} />
            {cart.length > 0 && (
              <span style={{ position: 'absolute', top: '-8px', right: '-12px', background: '#ef4444', color: 'white', borderRadius: '50%', width: '18px', height: '18px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px', fontWeight: 'bold' }}>
                {cart.length}
              </span>
            )}
          </Link>

          {/* Language Switcher */}
          <div className="avatar-dropdown" ref={langDropdownRef} style={{ marginRight: '15px' }}>
            <div className="avatar-btn" onClick={() => setLangDropdownOpen(!langDropdownOpen)} style={{ background: 'transparent', fontSize: '1.2rem', padding: '0 5px', color: isSolidNav ? '#0f172a' : 'white' }}>
              🌐
            </div>
            <div className={`dropdown-menu ${langDropdownOpen ? 'active' : ''}`} style={{ minWidth: '120px' }}>
              <div className="dropdown-item" onClick={() => { i18n.changeLanguage('vi'); setLangDropdownOpen(false); }}>
                🇻🇳 Tiếng Việt {i18n.language === 'vi' && '✓'}
              </div>
              <div className="dropdown-item" onClick={() => { i18n.changeLanguage('en'); setLangDropdownOpen(false); }}>
                🇺🇸 English {i18n.language === 'en' && '✓'}
              </div>
            </div>
          </div>

          {/* User Avatar / Login */}
          {isAuthenticated && user ? (
            <div className="avatar-dropdown" ref={dropdownRef}>
              <div className="avatar-btn" onClick={() => setDropdownOpen(!dropdownOpen)}>
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
                <div className="dropdown-item" onClick={() => { navigate('/profile', { state: { tab: 'bookings' } }); setDropdownOpen(false); }}>
                  <FaHistory /> {t('navbar.myBooking')}
                </div>
                <div className="dropdown-item" onClick={() => { navigate('/profile', { state: { tab: 'wishlist' } }); setDropdownOpen(false); }}>
                  <FaHeart /> {t('navbar.wishlist')}
                </div>
                
                <div className="dropdown-divider"></div>
                <div className="dropdown-item" onClick={handleLogout} style={{ color: 'var(--danger)' }}>
                  <FaSignOutAlt /> {t('navbar.logout')}
                </div>
              </div>
            </div>
          ) : (
            <Link to="/login" className="login-register-link" style={{color: isSolidNav ? '#0f172a' : 'white'}}>
              LOGIN OR REGISTER <FaUser style={{ marginLeft: '8px' }} />
            </Link>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

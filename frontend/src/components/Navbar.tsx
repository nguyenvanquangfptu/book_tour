import React, { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaSuitcase, FaShoppingCart, FaSearch, FaUser, FaHistory, FaHeart, FaSignOutAlt } from 'react-icons/fa';
import { useCart } from '../context/CartContext';
import '../styles/navbar.css';

const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const [scrolled, setScrolled] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  
  const { getCartCount } = useCart();
  
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || 'null');

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 50);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setDropdownOpen(false);
    navigate('/login');
  };

  const getInitials = (name: string) => {
    if (!name) return 'U';
    return name.charAt(0).toUpperCase();
  };

  return (
    <nav className={`navbar ${scrolled ? 'navbar-scrolled' : ''}`}>
      <div className="container navbar-container">
        {/* Brand */}
        <Link to="/" className="navbar-brand">
          <FaSuitcase className="brand-icon" />
          <span>BookingTour</span>
        </Link>

        {/* Menu Links */}
        <div className="navbar-menu">
          <Link to="/tours" className="nav-link">Tours</Link>
          <Link to="/about" className="nav-link">About</Link>
          <Link to="/contact" className="nav-link">Contact</Link>
        </div>

        {/* Search & Auth */}
        <div className="navbar-auth">
          {/* Search Bar */}
          <div className="navbar-search">
            <FaSearch className="navbar-search-icon" />
            <input 
              type="text" 
              placeholder="Search tours, city..." 
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  navigate(`/tours?keyword=${encodeURIComponent(e.currentTarget.value)}`);
                }
              }}
            />
          </div>

          <Link to="/cart" className="cart-icon-wrapper">
            <FaShoppingCart />
            {getCartCount() > 0 && (
              <span className="badge" style={{ position: 'absolute', top: '-10px', right: '-12px', background: 'var(--danger)', color: '#fff', fontSize: '0.7rem', padding: '2px 6px' }}>
                {getCartCount()}
              </span>
            )}
          </Link>

          {token && user ? (
            <div className="avatar-dropdown" ref={dropdownRef}>
              <div className="avatar-btn" onClick={() => setDropdownOpen(!dropdownOpen)}>
                {/* Random avatar from ui-avatars or unsplash. For now, use initials */}
                {user.avatar ? (
                  <img src={user.avatar} alt="Avatar" />
                ) : (
                  getInitials(user.username)
                )}
              </div>
              
              <div className={`dropdown-menu ${dropdownOpen ? 'active' : ''}`}>
                {user.role === 'ADMIN' && (
                  <>
                    <Link to="/admin/tours" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                      <span style={{ color: 'var(--danger)' }}>Dashboard</span>
                    </Link>
                    <div className="dropdown-divider"></div>
                  </>
                )}
                
                <Link to="/profile" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                  <FaUser /> Profile
                </Link>
                <div className="dropdown-item" onClick={() => { navigate('/profile'); setDropdownOpen(false); }}>
                  <FaHistory /> My Booking
                </div>
                <div className="dropdown-item" onClick={() => { navigate('/tours'); setDropdownOpen(false); }}>
                  <FaHeart /> Wishlist
                </div>
                <div className="dropdown-divider"></div>
                <div className="dropdown-item" onClick={handleLogout} style={{ color: 'var(--danger)' }}>
                  <FaSignOutAlt /> Logout
                </div>
              </div>
            </div>
          ) : (
            <>
              <Link to="/login" className="nav-link" style={{color: scrolled ? 'var(--text-primary)' : 'white'}}>Login</Link>
              <Link to="/register" className="btn btn-primary" style={{ padding: '8px 20px' }}>Sign up</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

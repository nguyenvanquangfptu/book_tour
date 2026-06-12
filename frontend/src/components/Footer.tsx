import React from 'react';
import { Link } from 'react-router-dom';
import { FaSuitcase, FaFacebookF, FaTwitter, FaInstagram, FaYoutube } from 'react-icons/fa';
import '../styles/footer.css';

const Footer: React.FC = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-grid">
          {/* Column 1: Brand & Desc */}
          <div className="footer-col">
            <Link to="/" className="footer-brand">
              <FaSuitcase /> BookingTour
            </Link>
            <p className="footer-desc">
              Discover Vietnam with our amazing tours. We provide the best experiences for your travel journey with top-notch services and affordable prices.
            </p>
          </div>

          {/* Column 2: Quick Links */}
          <div className="footer-col">
            <h3>Quick Links</h3>
            <ul className="footer-links">
              <li><Link to="/">Home</Link></li>
              <li><Link to="/tours">All Tours</Link></li>
              <li><Link to="/about">About Us</Link></li>
              <li><Link to="/contact">Contact</Link></li>
            </ul>
          </div>

          {/* Column 3: Support */}
          <div className="footer-col">
            <h3>Support</h3>
            <ul className="footer-links">
              <li><Link to="#">FAQ</Link></li>
              <li><Link to="#">Privacy Policy</Link></li>
              <li><Link to="#">Terms of Service</Link></li>
              <li><Link to="#">Refund Policy</Link></li>
            </ul>
          </div>

          {/* Column 4: Social & Newsletter */}
          <div className="footer-col">
            <h3>Connect With Us</h3>
            <div className="social-links">
              <a href="#" className="social-icon"><FaFacebookF /></a>
              <a href="#" className="social-icon"><FaTwitter /></a>
              <a href="#" className="social-icon"><FaInstagram /></a>
              <a href="#" className="social-icon"><FaYoutube /></a>
            </div>
            
            <h3 style={{ fontSize: '1.1rem', marginTop: '24px', marginBottom: '12px' }}>Newsletter</h3>
            <form className="newsletter-form" onSubmit={(e) => e.preventDefault()}>
              <input type="email" placeholder="Your email address" required />
              <button type="submit">Subscribe</button>
            </form>
          </div>
        </div>
      </div>
      
      <div className="footer-bottom">
        <div className="container">
          <p>&copy; {new Date().getFullYear()} BookingTour. All rights reserved. Designed with ❤️ in Vietnam.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

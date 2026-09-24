import React from 'react';
import { Link } from 'react-router-dom';
import { FaSuitcase, FaFacebookF, FaTwitter, FaInstagram, FaYoutube } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import '../styles/footer.css';

const Footer: React.FC = () => {
  const { t } = useTranslation();
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
              {t('footer.desc')}
            </p>
          </div>

          {/* Column 2: Quick Links */}
          <div className="footer-col">
            <h3>{t('footer.quickLinks')}</h3>
            <ul className="footer-links">
              <li><Link to="/">{t('footer.home')}</Link></li>
              <li><Link to="/tours">{t('footer.allTours')}</Link></li>
              <li><Link to="/about">{t('footer.aboutUs')}</Link></li>
              <li><Link to="/contact">{t('footer.contact')}</Link></li>
            </ul>
          </div>

          {/* Column 3: Support */}
          <div className="footer-col">
            <h3>{t('footer.support')}</h3>
            <ul className="footer-links">
              <li><Link to="/faq">{t('footer.faq')}</Link></li>
              <li><Link to="/privacy-policy">{t('footer.privacyPolicy')}</Link></li>
              <li><Link to="/terms-of-service">{t('footer.termsOfService')}</Link></li>
              <li><Link to="/refund-policy">{t('footer.refundPolicy')}</Link></li>
            </ul>
          </div>

          {/* Column 4: Social & Newsletter */}
          <div className="footer-col">
            <h3>{t('footer.connect')}</h3>
            <div className="social-links">
              <a href="#" className="social-icon"><FaFacebookF /></a>
              <a href="#" className="social-icon"><FaTwitter /></a>
              <a href="#" className="social-icon"><FaInstagram /></a>
              <a href="#" className="social-icon"><FaYoutube /></a>
            </div>
            
            <h3 style={{ fontSize: '1.1rem', marginTop: '24px', marginBottom: '12px' }}>{t('footer.newsletter')}</h3>
            <form className="newsletter-form" onSubmit={(e) => e.preventDefault()}>
              <input type="email" placeholder={t('footer.emailPlaceholder')} required />
              <button type="submit">{t('footer.subscribe')}</button>
            </form>
          </div>
        </div>
      </div>
      
      <div className="footer-bottom">
        <div className="container">
          <p>&copy; {new Date().getFullYear()} {t('footer.copyright')}</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

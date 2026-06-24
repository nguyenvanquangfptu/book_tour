import React, { useState } from 'react';
import { FaPhoneAlt, FaEnvelope, FaMapMarkerAlt } from 'react-icons/fa';
import Swal from 'sweetalert2';
import api from '../api/axiosConfig';
import { useTranslation } from 'react-i18next';
import '../styles/pages.css';

const ContactPage: React.FC = () => {
  const { t } = useTranslation();
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    subject: '',
    message: ''
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/contacts', formData);
      Swal.fire({
        icon: 'success',
        title: t('contact.successTitle'),
        text: t('contact.successText'),
        confirmButtonColor: '#3b82f6'
      });
      setFormData({ fullName: '', email: '', subject: '', message: '' });
    } catch (error) {
      Swal.fire({
        icon: 'error',
        title: t('contact.errorTitle'),
        text: t('contact.errorText'),
        confirmButtonColor: '#ef4444'
      });
    } finally {
      setLoading(false);
    }
  };
  return (
    <div className="page-wrapper">
      <div className="page-header" style={{ backgroundImage: "url('https://images.unsplash.com/photo-1516738901171-8eb4fc13bd20?auto=format&fit=crop&w=1920&q=80')" }}>
        <div className="page-overlay"></div>
        <h1 className="page-title animate-fade-in">{t('contact.pageTitle')}</h1>
      </div>

      <div className="contact-section container">
        <div className="contact-grid">
          {/* Contact Info */}
          <div className="contact-info-card animate-slide-up">
            <h2 style={{ fontSize: '2rem', marginBottom: '32px', color: 'var(--primary-dark)' }}>{t('contact.contactInfo')}</h2>
            
            <div className="contact-item">
              <FaMapMarkerAlt className="contact-icon" />
              <div>
                <h4>{t('contact.officeAddress')}</h4>
                <p>{t('contact.addressLine1')}<br/>{t('contact.addressLine2')}</p>
              </div>
            </div>

            <div className="contact-item">
              <FaPhoneAlt className="contact-icon" />
              <div>
                <h4>{t('contact.hotline')}</h4>
                <p>+84 123 456 789<br/>+84 987 654 321</p>
              </div>
            </div>

            <div className="contact-item">
              <FaEnvelope className="contact-icon" />
              <div>
                <h4>{t('contact.email')}</h4>
                <p>vanquangqn28@gmail.com</p>
              </div>
            </div>

            {/* Google Map Mock */}
            <div style={{ marginTop: '40px', height: '250px', background: '#e2e8f0', borderRadius: '15px', overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
               <img src="https://images.unsplash.com/photo-1524661135-423995f22d0b?auto=format&fit=crop&w=800&q=80" alt="Map" style={{width: '100%', height: '100%', objectFit: 'cover', opacity: 0.8}} />
            </div>
          </div>

          {/* Contact Form */}
          <div className="contact-form animate-slide-up" style={{ animationDelay: '0.2s' }}>
            <h2 style={{ fontSize: '2rem', marginBottom: '16px', color: 'var(--primary-dark)' }}>{t('contact.sendMessage')}</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '32px' }}>{t('contact.formDesc')}</p>

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>{t('contact.fullNameLabel')}</label>
                <input type="text" name="fullName" className="form-control" placeholder={t('contact.fullNamePlaceholder')} value={formData.fullName} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>{t('contact.emailLabel')}</label>
                <input type="email" name="email" className="form-control" placeholder={t('contact.emailPlaceholder')} value={formData.email} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>{t('contact.subjectLabel')}</label>
                <input type="text" name="subject" className="form-control" placeholder={t('contact.subjectPlaceholder')} value={formData.subject} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>{t('contact.messageLabel')}</label>
                <textarea name="message" className="form-control" placeholder={t('contact.messagePlaceholder')} value={formData.message} onChange={handleChange} required></textarea>
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '16px', fontSize: '1.1rem' }} disabled={loading}>
                {loading ? t('contact.sending') : t('contact.sendBtn')}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContactPage;

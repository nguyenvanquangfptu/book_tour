import React from 'react';
import '../styles/pages.css';
import { useTranslation } from 'react-i18next';

const FAQPage: React.FC = () => {
  const { t } = useTranslation();

  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ background: 'white', padding: '40px', borderRadius: '16px', boxShadow: 'var(--shadow-sm)', marginBottom: '40px' }}>
        <h1 style={{ marginBottom: '32px', color: 'var(--primary-dark)', fontSize: '2.5rem' }}>{t('faq.title')}</h1>
        
        <div style={{ marginBottom: '24px' }}>
          <h3 style={{ marginBottom: '12px', color: 'var(--text-primary)' }}>{t('faq.q1')}</h3>
          <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>{t('faq.a1')}</p>
        </div>

        <div style={{ marginBottom: '24px' }}>
          <h3 style={{ marginBottom: '12px', color: 'var(--text-primary)' }}>{t('faq.q2')}</h3>
          <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>{t('faq.a2')}</p>
        </div>

        <div style={{ marginBottom: '24px' }}>
          <h3 style={{ marginBottom: '12px', color: 'var(--text-primary)' }}>{t('faq.q3')}</h3>
          <p style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>{t('faq.a3')}</p>
        </div>
      </div>
    </div>
  );
};

export default FAQPage;

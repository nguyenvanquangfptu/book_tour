import React, { useState } from 'react';
import '../styles/pages.css';
import { useTranslation } from 'react-i18next';
import { FaChevronDown, FaChevronUp, FaQuestionCircle } from 'react-icons/fa';

const FAQPage: React.FC = () => {
  const { t } = useTranslation();
  const [openIndex, setOpenIndex] = useState<number | null>(0);

  const faqs = [
    { q: t('profile.faq.q1'), a: t('profile.faq.a1') },
    { q: t('profile.faq.q2'), a: t('profile.faq.a2') },
    { q: t('profile.faq.q3'), a: t('profile.faq.a3') }
  ];

  return (
    <div className="page-wrapper" style={{ paddingTop: '100px', minHeight: '100vh', background: 'var(--bg-alt)' }}>
      <div className="container" style={{ maxWidth: '800px', margin: '0 auto', padding: '0 20px' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '50px' }}>
          <div style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: '80px', height: '80px', borderRadius: '50%', background: 'rgba(37, 99, 235, 0.1)', color: 'var(--primary-dark)', marginBottom: '24px' }}>
            <FaQuestionCircle size={40} />
          </div>
          <h1 style={{ color: 'var(--primary-dark)', fontSize: '3rem', fontWeight: '800', marginBottom: '16px' }}>{t('profile.faq.title')}</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '1.2rem', maxWidth: '600px', margin: '0 auto', lineHeight: '1.6' }}>
            Tìm câu trả lời cho những câu hỏi thường gặp nhất về việc đặt tour và thanh toán.
          </p>
        </div>

        <div style={{ background: 'white', borderRadius: '24px', boxShadow: '0 20px 40px rgba(0,0,0,0.05)', overflow: 'hidden', marginBottom: '80px' }}>
          {faqs.map((faq, index) => (
            <div key={index} style={{ borderBottom: index < faqs.length - 1 ? '1px solid var(--border-color)' : 'none' }}>
              <button 
                onClick={() => setOpenIndex(openIndex === index ? null : index)}
                style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '24px 32px', background: 'transparent', border: 'none', cursor: 'pointer', textAlign: 'left', outline: 'none' }}
              >
                <h3 style={{ fontSize: '1.2rem', fontWeight: openIndex === index ? '700' : '600', color: openIndex === index ? 'var(--primary-dark)' : 'var(--text-primary)', margin: 0, transition: 'color 0.3s' }}>
                  {faq.q}
                </h3>
                <div style={{ color: openIndex === index ? 'var(--primary-dark)' : 'var(--text-secondary)', transition: 'all 0.3s' }}>
                  {openIndex === index ? <FaChevronUp /> : <FaChevronDown />}
                </div>
              </button>
              
              <div style={{ maxHeight: openIndex === index ? '500px' : '0', overflow: 'hidden', transition: 'max-height 0.4s ease-in-out' }}>
                <div style={{ padding: '0 32px 32px 32px', color: 'var(--text-secondary)', lineHeight: '1.8', fontSize: '1.05rem' }}>
                  {faq.a}
                </div>
              </div>
            </div>
          ))}
        </div>
        
      </div>
    </div>
  );
};

export default FAQPage;

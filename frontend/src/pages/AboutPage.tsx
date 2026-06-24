import React from 'react';
import { useTranslation } from 'react-i18next';
import '../styles/pages.css';

const AboutPage: React.FC = () => {
  const { t } = useTranslation();
  const teamMembers = [
    {
      name: 'Nguyễn Văn A',
      role: t('about.team.ceo'),
      image: 'https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=300&q=80'
    },
    {
      name: 'Trần Thị B',
      role: t('about.team.manager'),
      image: 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=80'
    },
    {
      name: 'Lê Văn C',
      role: t('about.team.designer'),
      image: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=300&q=80'
    },
    {
      name: 'Phạm Thị D',
      role: t('about.team.support'),
      image: 'https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&w=300&q=80'
    }
  ];

  return (
    <div className="page-wrapper">
      {/* Page Header */}
      <div className="page-header" style={{ backgroundImage: "url('https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=1920&q=80')" }}>
        <div className="page-overlay"></div>
        <h1 className="page-title animate-fade-in">{t('about.title')}</h1>
      </div>

      {/* About Section */}
      <div className="about-section container">
        <div className="about-grid">
          <div className="about-content animate-slide-up">
            <h2>{t('about.journeyTitle')}</h2>
            <p>{t('about.desc1')}</p>
            <p>{t('about.desc2')}</p>
            <div style={{ marginTop: '32px', display: 'flex', gap: '24px' }}>
              <div>
                <h3 style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '8px' }}>10k+</h3>
                <p style={{ color: 'var(--text-secondary)', fontWeight: '600' }}>{t('about.statsCustomers')}</p>
              </div>
              <div>
                <h3 style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '8px' }}>500+</h3>
                <p style={{ color: 'var(--text-secondary)', fontWeight: '600' }}>{t('about.statsDestinations')}</p>
              </div>
            </div>
          </div>
          <div className="about-image animate-slide-up" style={{ animationDelay: '0.2s' }}>
            <img src="https://images.unsplash.com/photo-1539635278303-d4002c07eae3?auto=format&fit=crop&w=800&q=80" alt="Travel Experience" />
          </div>
        </div>

        {/* Why Choose Us - Reuse from home features */}
        <div style={{ marginTop: '100px', textAlign: 'center' }}>
          <h2 style={{ fontSize: '2.5rem', color: 'var(--primary-dark)', marginBottom: '48px' }}>{t('about.whyChooseUs')}</h2>
          <div className="features-grid">
            <div className="feature-card glass-card">
              <h3>{t('about.bestPrice')}</h3>
              <p>{t('about.bestPriceDesc')}</p>
            </div>
            <div className="feature-card glass-card">
              <h3>{t('about.fiveStarService')}</h3>
              <p>{t('about.fiveStarServiceDesc')}</p>
            </div>
            <div className="feature-card glass-card">
              <h3>{t('about.support247')}</h3>
              <p>{t('about.support247Desc')}</p>
            </div>
          </div>
        </div>

        {/* Team Section */}
        <div style={{ marginTop: '100px' }}>
          <h2 style={{ fontSize: '2.5rem', color: 'var(--primary-dark)', marginBottom: '16px', textAlign: 'center' }}>{t('about.ourTeam')}</h2>
          <p style={{ textAlign: 'center', color: 'var(--text-secondary)', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto' }}>
            {t('about.ourTeamDesc')}
          </p>
          
          <div className="team-grid">
            {teamMembers.map((member, index) => (
              <div key={index} className="team-member animate-slide-up" style={{ animationDelay: `${index * 0.1}s` }}>
                <img src={member.image} alt={member.name} />
                <h4>{member.name}</h4>
                <p>{member.role}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AboutPage;

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaMapMarkerAlt, FaCalendarAlt, FaSearch, FaWallet } from 'react-icons/fa';
import { useQuery } from '@tanstack/react-query';
import TourCard from '../components/TourCard';
import { TourService } from '../services/TourService';
import { ReviewService } from '../services/ReviewService';
import { useTourOptions } from '../hooks/useTourOptions';
import { useTours } from '../hooks/useTours';
import { useTranslation } from 'react-i18next';
import '../styles/pages.css';
import '../styles/home.css';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  
  // Search States
  const [destination, setDestination] = useState('');
  const [duration, setDuration] = useState('');
  const [budget, setBudget] = useState('');

  // 1. Fetch Popular Destinations
  const { data: popularDestinations = [] } = useQuery({
    queryKey: ['popularDestinations'],
    queryFn: () => TourService.getPopularDestinations(4),
  });

  // 2. Fetch Tour Options (Cached globally)
  const { data: tourOptions } = useTourOptions();
  const allDestinations = tourOptions?.destinations || [];

  // 3. Fetch Featured Tours
  const { data: toursData, isLoading: toursLoading } = useTours({ page: 0, size: 6 });
  const tours = toursData?.content || [];

  // 4. Fetch Recent Reviews
  const { data: recentReviews = [] } = useQuery({
    queryKey: ['recentReviews'],
    queryFn: () => ReviewService.getRecentReviews(),
  });

  const handleSearchClick = () => {
    navigate(`/tours?dest=${destination}`);
  };

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-overlay"></div>
        <div className="hero-content animate-fade-up">
          <h1 className="hero-title">{t('home.heroTitle')}</h1>
          <p className="hero-subtitle">{t('home.heroSubtitle')}</p>
          
          <div className="hero-search-box">
            <div className="search-input-group">
              <FaMapMarkerAlt className="search-icon" />
              <div className="search-input-wrapper">
                <label>{t('home.destination')}</label>
                <select 
                  className="search-input" 
                  value={destination} 
                  onChange={(e) => setDestination(e.target.value)}
                >
                  <option value="">Where are you going?</option>
                  {allDestinations.map(dest => (
                    <option key={dest} value={dest}>{dest}</option>
                  ))}
                </select>
              </div>
            </div>
            
            <div className="search-divider"></div>
            
            <div className="search-input-group">
              <FaCalendarAlt className="search-icon" />
              <div className="search-input-wrapper">
                <label>{t('home.duration')}</label>
                <select 
                  className="search-input" 
                  value={duration} 
                  onChange={(e) => setDuration(e.target.value)}
                >
                  <option value="">Any</option>
                  <option value="1-3">1 - 3 Days</option>
                  <option value="4-7">4 - 7 Days</option>
                  <option value="8+">8+ Days</option>
                </select>
              </div>
            </div>

            <div className="search-divider"></div>

            <div className="search-input-group">
              <FaWallet className="search-icon" />
              <div className="search-input-wrapper">
                <label>{t('home.budget')}</label>
                <select 
                  className="search-input" 
                  value={budget} 
                  onChange={(e) => setBudget(e.target.value)}
                >
                  <option value="">Any Budget</option>
                  <option value="low">Under 2.000.000đ</option>
                  <option value="mid">2M - 5M VND</option>
                  <option value="high">Above 5.000.000đ</option>
                </select>
              </div>
            </div>
            
            <button className="btn btn-primary search-btn" onClick={handleSearchClick}>
              <FaSearch /> {t('home.search')}
            </button>
          </div>
        </div>
      </section>

      {/* Popular Destinations */}
      <section className="destinations-section">
        <div className="container">
          <div className="section-header animate-fade-up">
            <h2 className="section-title">{t('home.popularDestinations')}</h2>
            <p className="section-subtitle">{t('home.exploreDestinations')}</p>
          </div>
          
          <div className="destinations-grid">
            {popularDestinations.length > 0 ? popularDestinations.map((dest: any, index: number) => (
              <div className="destination-card animate-fade-up" style={{ animationDelay: `${index * 0.1}s` }} key={index} onClick={() => navigate(`/tours?dest=${dest.name}`)}>
                <img 
                  src={dest.image || 'https://images.unsplash.com/photo-1596700055745-f0bbbb3d2b0e?auto=format&fit=crop&w=800&q=80'} 
                  alt={dest.name} 
                  className="destination-img" 
                  onError={(e) => {
                    (e.target as HTMLImageElement).src = 'https://images.unsplash.com/photo-1596700055745-f0bbbb3d2b0e?auto=format&fit=crop&w=800&q=80';
                  }}
                />
                <div className="destination-overlay">
                  <h3 className="destination-name">{dest.name}</h3>
                  <span className="destination-tours">{t('home.toursCount', { count: dest.count })}</span>
                </div>
              </div>
            )) : (
              <div style={{ textAlign: 'center', gridColumn: '1 / -1', padding: '40px 0' }}>
                <p>{t('home.noDestinations')}</p>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Featured Tours */}
      <section className="tours-section">
        <div className="container">
          <div className="section-header animate-fade-up">
            <h2 className="section-title">{t('home.featuredTours')}</h2>
            <p className="section-subtitle">{t('home.handpickedTours')}</p>
          </div>

          {toursLoading ? (
            <div className="tours-grid">
              {[1, 2, 3, 4, 5, 6].map(n => (
                <div key={n} style={{ height: '400px', background: 'var(--border-color)', borderRadius: '16px', animation: 'pulse 1.5s infinite' }}></div>
              ))}
            </div>
          ) : tours.length > 0 ? (
            <div className="tours-grid">
              {tours.map((tour, index) => (
                <div key={tour.id} style={{ animationDelay: `${index * 0.1}s` }}>
                  <TourCard tour={tour} />
                </div>
              ))}
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '40px 0' }}>
              <p>{t('home.noFeaturedTours')}</p>
            </div>
          )}
          
          <div style={{ textAlign: 'center', marginTop: '48px' }}>
            <button className="btn btn-outline" style={{ padding: '12px 32px' }} onClick={() => navigate('/tours')}>
              {t('home.viewAllTours')}
            </button>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="testimonials-section">
        <div className="container">
          <div className="section-header animate-fade-up">
            <h2 className="section-title">{t('home.testimonials')}</h2>
            <p className="section-subtitle">{t('home.realExperiences')}</p>
          </div>
          
          <div className="testimonials-grid">
            {recentReviews.length > 0 ? (
              recentReviews.slice(0, 3).map((review, idx) => (
                <div key={review.id} className="testimonial-card animate-fade-up" style={{ animationDelay: `${idx * 0.1}s` }}>
                  <div className="stars">{'⭐'.repeat(review.rating)}</div>
                  <p className="quote">"{review.comment}"</p>
                  <h4 className="author">- {review.username}</h4>
                </div>
              ))
            ) : (
              <>
                <div className="testimonial-card animate-fade-up">
                  <div className="stars">⭐⭐⭐⭐⭐</div>
                  <p className="quote">"{t('home.fallbackReview1')}"</p>
                  <h4 className="author">- Nguyễn Văn A</h4>
                </div>
                <div className="testimonial-card animate-fade-up" style={{ animationDelay: '0.1s' }}>
                  <div className="stars">⭐⭐⭐⭐⭐</div>
                  <p className="quote">"{t('home.fallbackReview2')}"</p>
                  <h4 className="author">- Trần Thị B</h4>
                </div>
                <div className="testimonial-card animate-fade-up" style={{ animationDelay: '0.2s' }}>
                  <div className="stars">⭐⭐⭐⭐⭐</div>
                  <p className="quote">"{t('home.fallbackReview3')}"</p>
                  <h4 className="author">- John Doe</h4>
                </div>
              </>
            )}
          </div>
        </div>
      </section>
    </div>
  );
};

export default Home;

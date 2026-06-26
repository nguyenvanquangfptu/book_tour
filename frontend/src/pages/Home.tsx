import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaSearch } from 'react-icons/fa';
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
  const [destInputValue, setDestInputValue] = useState('');
  const [showDestDropdown, setShowDestDropdown] = useState(false);
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [guests, setGuests] = useState('2');
  
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
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-overlay"></div>
        <div className="hero-content animate-fade-up">
          <h1 className="hero-title">BookTour</h1>
          
          <div className="hero-search-container">
            <div className="hero-search-box">
              <div className="search-input-group" style={{ position: 'relative' }}>
                <div className="search-input-wrapper">
                  <label>{t('searchBar.where')}</label>
                  <input 
                    type="text" 
                    className="search-input" 
                    placeholder={t('searchBar.searchDestinations')}
                    value={destInputValue}
                    onChange={(e) => setDestInputValue(e.target.value)}
                    onFocus={(e) => {
                      e.target.select();
                      setShowDestDropdown(true);
                    }}
                    onBlur={() => setTimeout(() => setShowDestDropdown(false), 200)}
                  />
                  {showDestDropdown && allDestinations.length > 0 && (
                    <div style={{ position: 'absolute', top: '100%', left: 0, right: 0, background: 'white', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', zIndex: 10, maxHeight: '200px', overflowY: 'auto', marginTop: '8px' }}>
                      {allDestinations
                        .filter((d: string) => destInputValue === '' || allDestinations.includes(destInputValue) || d.toLowerCase().includes(destInputValue.toLowerCase()))
                        .map((dest: string) => (
                          <div 
                            key={dest}
                            style={{ padding: '10px 16px', cursor: 'pointer', borderBottom: '1px solid #f1f5f9', color: '#0f172a', textAlign: 'left', fontWeight: 600, fontSize: '0.9rem' }}
                            onMouseDown={() => { setDestInputValue(dest); }}
                          >
                            📍 {dest}
                          </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
              <div className="search-divider"></div>
              <div className="search-input-group">
                <div className="search-input-wrapper">
                  <label>{t('searchBar.departure')}</label>
                  <input type="date" className="search-input" min={new Date().toISOString().split('T')[0]} value={checkIn} onChange={(e) => setCheckIn(e.target.value)} />
                </div>
              </div>
              <div className="search-divider"></div>
              <div className="search-input-group">
                <div className="search-input-wrapper">
                  <label>{t('searchBar.return')}</label>
                  <input type="date" className="search-input" min={checkIn || new Date().toISOString().split('T')[0]} value={checkOut} onChange={(e) => setCheckOut(e.target.value)} />
                </div>
              </div>
              <div className="search-divider"></div>
              <div className="search-input-group">
                <div className="search-input-wrapper">
                  <label>{t('searchBar.guests')}</label>
                  <input type="number" min="1" className="search-input" value={guests} onChange={(e) => setGuests(e.target.value)} />
                </div>
              </div>
              
              <button className="hero-search-btn-circular" onClick={handleSearchClick}>
                <FaSearch color="white" />
              </button>
            </div>

            <div className="hero-dest-tags">
              {popularDestinations.slice(0, 5).map((dest: any) => (
                <span key={dest.id || dest.name} onClick={() => navigate(`/tours?destination=${dest.id}`)}>
                  {dest.name.toUpperCase()}
                </span>
              ))}
            </div>
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

      {/* Hot Deals */}
      <section className="tours-section">
        <div className="container">
          <div className="section-header animate-fade-up" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h2 className="section-title" style={{ textAlign: 'left', marginBottom: '0' }}>Hot Deals</h2>
            </div>
            <div style={{ display: 'flex', gap: '10px' }}>
              <button className="nav-arrow-btn">{'<'}</button>
              <button className="nav-arrow-btn">{'>'}</button>
            </div>
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

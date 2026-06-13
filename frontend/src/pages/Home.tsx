import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaMapMarkerAlt, FaCalendarAlt, FaSearch, FaWallet } from 'react-icons/fa';
import TourCard from '../components/TourCard';
import { TourService } from '../services/TourService';
import { ReviewService, type ReviewResponse } from '../services/ReviewService';
import '../styles/pages.css';
import '../styles/home.css';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [tours, setTours] = useState<any[]>([]);
  const [popularDestinations, setPopularDestinations] = useState<any[]>([]);
  const [recentReviews, setRecentReviews] = useState<ReviewResponse[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Search States
  const [destination, setDestination] = useState('');
  const [duration, setDuration] = useState('');
  const [budget, setBudget] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        // Fetch popular destinations
        const destData = await TourService.getPopularDestinations(4);
        if (destData) {
          setPopularDestinations(destData);
        }

        // Using getTours as a placeholder for getFeaturedTours
        const toursData = await TourService.getTours(0, 6, '', '', '', '', undefined, 'id', 'DESC');
        setTours(toursData.content || []);
        
        const reviewsData = await ReviewService.getRecentReviews();
        setRecentReviews(reviewsData || []);
      } catch (error) {
        console.error('Failed to fetch data for home page:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleSearchClick = () => {
    // Navigate to ToursPage with query params
    navigate(`/tours?dest=${destination}`);
  };

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-overlay"></div>
        <div className="hero-content animate-fade-up">
          <h1 className="hero-title">Discover Vietnam With Amazing Tours</h1>
          <p className="hero-subtitle">Find beaches, mountains and unforgettable experiences</p>
          
          <div className="hero-search-box">
            <div className="search-input-group">
              <FaMapMarkerAlt className="search-icon" />
              <div className="search-input-wrapper">
                <label>Destination</label>
                <select 
                  className="search-input" 
                  value={destination} 
                  onChange={(e) => setDestination(e.target.value)}
                >
                  <option value="">Where are you going?</option>
                  <option value="Hà Nội">Hà Nội</option>
                  <option value="Sapa">Sapa</option>
                  <option value="Hạ Long">Hạ Long</option>
                  <option value="Đà Nẵng">Đà Nẵng</option>
                  <option value="Hội An">Hội An</option>
                  <option value="Nha Trang">Nha Trang</option>
                  <option value="Đà Lạt">Đà Lạt</option>
                  <option value="Phú Quốc">Phú Quốc</option>
                </select>
              </div>
            </div>
            
            <div className="search-divider"></div>
            
            <div className="search-input-group">
              <FaCalendarAlt className="search-icon" />
              <div className="search-input-wrapper">
                <label>Duration</label>
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
                <label>Budget</label>
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
              <FaSearch /> Search
            </button>
          </div>
        </div>
      </section>

      {/* Popular Destinations */}
      <section className="destinations-section">
        <div className="container">
          <div className="section-header animate-fade-up">
            <h2 className="section-title">Popular Destinations</h2>
            <p className="section-subtitle">Explore our most booked cities and places</p>
          </div>
          
          <div className="destinations-grid">
            {popularDestinations.length > 0 ? popularDestinations.map((dest, index) => (
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
                  <span className="destination-tours">{dest.count} Tours</span>
                </div>
              </div>
            )) : (
              <div style={{ textAlign: 'center', gridColumn: '1 / -1', padding: '40px 0' }}>
                <p>No popular destinations found.</p>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Featured Tours */}
      <section className="tours-section">
        <div className="container">
          <div className="section-header animate-fade-up">
            <h2 className="section-title">Featured Tours</h2>
            <p className="section-subtitle">Handpicked selection of the best tours for you</p>
          </div>

          {loading ? (
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
              <p>No featured tours available at the moment.</p>
            </div>
          )}
          
          <div style={{ textAlign: 'center', marginTop: '48px' }}>
            <button className="btn btn-outline" style={{ padding: '12px 32px' }} onClick={() => navigate('/tours')}>
              View All Tours
            </button>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="testimonials-section">
        <div className="container">
          <div className="section-header animate-fade-up">
            <h2 className="section-title">What Our Travelers Say</h2>
            <p className="section-subtitle">Real experiences from our amazing customers</p>
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
                  <p className="quote">"What an amazing experience! The guide was extremely knowledgeable and the views in Ha Long Bay were breathtaking. Highly recommended!"</p>
                  <h4 className="author">- Nguyễn Văn A</h4>
                </div>
                <div className="testimonial-card animate-fade-up" style={{ animationDelay: '0.1s' }}>
                  <div className="stars">⭐⭐⭐⭐⭐</div>
                  <p className="quote">"Dịch vụ 5 sao tuyệt vời. Mọi thứ từ đưa đón, khách sạn đến ăn uống đều được chuẩn bị chu đáo. BookingTour làm việc rất chuyên nghiệp."</p>
                  <h4 className="author">- Trần Thị B</h4>
                </div>
                <div className="testimonial-card animate-fade-up" style={{ animationDelay: '0.2s' }}>
                  <div className="stars">⭐⭐⭐⭐⭐</div>
                  <p className="quote">"My family had the best vacation ever in Phu Quoc. The booking process was seamless and the support team was very helpful."</p>
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

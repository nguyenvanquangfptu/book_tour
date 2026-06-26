import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaRegHeart } from 'react-icons/fa';
import { formatPrice } from '../utils/formatPrice';
import '../styles/tourCard.css';

interface TourProps {
  tour: any;
}

const TourCard: React.FC<TourProps> = ({ tour }) => {
  const navigate = useNavigate();
  const [hasError, setHasError] = useState(false);

  const getFallbackImage = () => {
    // A nice travel fallback image
    return 'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=1000&q=80';
  };

  return (
    <div className="tour-card animate-fade-up">
      <div className="tour-img-container">
        <div className="tour-wishlist-icon">
          <FaRegHeart />
        </div>
        <img 
          src={hasError ? getFallbackImage() : (tour.imageUrl || getFallbackImage())} 
          alt={tour.title} 
          className="tour-img" 
          onError={() => {
            if (!hasError) {
              setHasError(true);
            }
          }}
        />
        <div className="tour-carousel-dots">
          <span className="dot active"></span>
          <span className="dot"></span>
          <span className="dot"></span>
          <span className="dot"></span>
        </div>
      </div>

      <div className="tour-info">
        <div className="tour-header-row">
          <div className="tour-location">
            {tour.destination ? tour.destination.toUpperCase() : 'DUBAI, EMIRATES'}
          </div>
          <div className="tour-rating">
            {'★'.repeat(Math.round(tour.rating || 5))} <span style={{ color: '#0f172a' }}>{tour.rating || 5}</span>
          </div>
        </div>
        
        <h3 className="tour-title" title={tour.title}>{tour.title}</h3>

        <div className="tour-dates">
          {tour.duration || '5 Ngày 4 Đêm'}
        </div>

        <div className="tour-footer">
          <div className="tour-price">
            {tour.price ? formatPrice(tour.price) : '0 đ'}
          </div>
          <button 
            className="btn btn-book-pill" 
            onClick={() => navigate(`/tours/${tour.id}`)}
          >
            BOOK NOW
          </button>
        </div>
      </div>
    </div>
  );
};

export default TourCard;

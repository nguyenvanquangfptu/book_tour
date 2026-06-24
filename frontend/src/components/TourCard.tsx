import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaMapMarkerAlt, FaStar, FaUsers, FaBus, FaHotel, FaUtensils } from 'react-icons/fa';
import { formatPrice } from '../utils/formatPrice';
import { useTranslation } from 'react-i18next';
import '../styles/tourCard.css';

interface TourProps {
  tour: any;
}

const TourCard: React.FC<TourProps> = ({ tour }) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [hasError, setHasError] = useState(false);

  const getFallbackImage = () => {
    // A nice travel fallback image
    return 'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=1000&q=80';
  };

  // Mock data for missing fields
  const mockOldPrice = tour.price * 1.25; // 20% discount

  return (
    <div className="tour-card animate-fade-up">
      <div className="tour-img-container">
        {/* Discount Badge */}
        <div className="tour-discount-badge">
          {t('tourCard.discount')}
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

        {/* Price Badge */}
        <div className="tour-price-badge">
          <span className="tour-price-old">
            {formatPrice(mockOldPrice)}
          </span>
          <span>
            {formatPrice(tour.price)}
          </span>
        </div>
      </div>

      <div className="tour-info">
        <div className="tour-destination">
          <FaMapMarkerAlt /> {tour.destination || t('tourCard.defaultDestination')}
        </div>
        
        <h3 className="tour-title" title={tour.title}>{tour.title}</h3>

        <div className="tour-stats">
          <div className="tour-stat-item rating">
            <FaStar /> {(tour.rating || 0).toFixed(1)} <span>({tour.reviewCount || 0} {t('tourCard.reviews')})</span>
          </div>
          <div className="tour-stat-item">
            <FaUsers /> {tour.bookedCount || 0} {t('tourCard.booked')}
          </div>
        </div>

        {/* Included Services Mock */}
        <div className="tour-services">
          <div className="service-icon" title={t('tourCard.transport')}><FaBus /></div>
          <div className="service-icon" title={t('tourCard.hotel')}><FaHotel /></div>
          <div className="service-icon" title={t('tourCard.meals')}><FaUtensils /></div>
        </div>

        <div className="tour-actions">
          <button 
            className="btn btn-outline" 
            onClick={() => navigate(`/tours/${tour.id}`)}
          >
            {t('tourCard.details')}
          </button>
          <button 
            className="btn btn-book" 
            onClick={() => navigate(`/tours/${tour.id}`)}
          >
            {t('tourCard.bookNow')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default TourCard;

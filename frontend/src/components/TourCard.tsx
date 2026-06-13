import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaMapMarkerAlt, FaStar, FaUsers, FaBus, FaHotel, FaUtensils } from 'react-icons/fa';
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

  // Mock data for missing fields
  const mockOldPrice = tour.price * 1.25; // 20% discount

  return (
    <div className="tour-card animate-fade-up">
      <div className="tour-img-container">
        {/* Discount Badge */}
        <div className="tour-discount-badge">
          🔥 20% OFF
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
            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(mockOldPrice)}
          </span>
          <span>
            {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(tour.price)}
          </span>
        </div>
      </div>

      <div className="tour-info">
        <div className="tour-destination">
          <FaMapMarkerAlt /> {tour.destination || 'Việt Nam'}
        </div>
        
        <h3 className="tour-title" title={tour.title}>{tour.title}</h3>

        <div className="tour-stats">
          <div className="tour-stat-item rating">
            <FaStar /> {(tour.rating || 0).toFixed(1)} <span>({tour.reviewCount || 0} reviews)</span>
          </div>
          <div className="tour-stat-item">
            <FaUsers /> {tour.bookedCount || 0} booked
          </div>
        </div>

        {/* Included Services Mock */}
        <div className="tour-services">
          <div className="service-icon" title="Transport"><FaBus /></div>
          <div className="service-icon" title="Hotel"><FaHotel /></div>
          <div className="service-icon" title="Meals"><FaUtensils /></div>
        </div>

        <div className="tour-actions">
          <button 
            className="btn btn-outline" 
            onClick={() => navigate(`/tours/${tour.id}`)}
          >
            Chi Tiết
          </button>
          <button 
            className="btn btn-book" 
            onClick={() => navigate(`/tours/${tour.id}`)}
          >
            Đặt Ngay
          </button>
        </div>
      </div>
    </div>
  );
};

export default TourCard;

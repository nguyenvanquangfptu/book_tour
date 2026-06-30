import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { FaRegHeart, FaHeart } from 'react-icons/fa';
import { formatPrice } from '../utils/formatPrice';
import { useWishlistStore } from '../store/useWishlistStore';
import '../styles/tourCard.css';

interface TourProps {
  tour: any;
}

const TourCard: React.FC<TourProps> = ({ tour }) => {
  const navigate = useNavigate();
  const [hasError, setHasError] = useState(false);
  const { isInWishlist, toggleWishlist } = useWishlistStore();

  const handleWishlistClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    toggleWishlist({
      id: tour.id,
      title: tour.title,
      price: tour.price,
      imageUrl: tour.imageUrl || getFallbackImage(),
      destination: tour.destination,
      duration: tour.duration
    });
  };

  const getFallbackImage = () => {
    // A nice travel fallback image
    return 'https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?auto=format&fit=crop&w=1000&q=80';
  };

  const images = tour.images && tour.images.length > 0 ? [...tour.images] : [];
  if (tour.imageUrl && !images.includes(tour.imageUrl)) {
    images.unshift(tour.imageUrl);
  }
  if (images.length === 0) {
    images.push(getFallbackImage());
  }

  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const hoverIntervalRef = useRef<any>(null);

  const handleMouseEnter = () => {
    if (images.length > 1 && !hoverIntervalRef.current) {
      hoverIntervalRef.current = setInterval(() => {
        setCurrentImageIndex((prev) => (prev + 1) % images.length);
        setHasError(false);
      }, 1200);
    }
  };

  const handleMouseLeave = () => {
    if (hoverIntervalRef.current) {
      clearInterval(hoverIntervalRef.current);
      hoverIntervalRef.current = null;
    }
    setCurrentImageIndex(0); // Trở về ảnh đầu tiên khi ngừng hover
  };

  useEffect(() => {
    return () => {
      if (hoverIntervalRef.current) {
        clearInterval(hoverIntervalRef.current);
      }
    };
  }, []);

  return (
    <div className="tour-card animate-fade-up" onClick={() => navigate(`/tours/${tour.id}`)}>
      <div 
        className="tour-img-container" 
        onMouseEnter={handleMouseEnter} 
        onMouseLeave={handleMouseLeave}
      >
        <div className="tour-wishlist-icon" onClick={handleWishlistClick}>
          {isInWishlist(tour.id) ? <FaHeart style={{color: '#ef4444'}} /> : <FaRegHeart />}
        </div>
        <img 
          src={hasError ? getFallbackImage() : images[currentImageIndex]} 
          alt={tour.title} 
          className="tour-img" 
          onError={() => {
            if (!hasError) {
              setHasError(true);
            }
          }}
        />
        {images.length > 1 && (
          <div className="tour-carousel-dots">
            {images.map((_, idx) => (
              <span 
                key={idx} 
                className={`dot ${idx === currentImageIndex ? 'active' : ''}`}
                onClick={(e) => {
                  e.stopPropagation();
                  setCurrentImageIndex(idx);
                  setHasError(false);
                }}
              ></span>
            ))}
          </div>
        )}
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
            onClick={(e) => {
              e.stopPropagation();
              navigate(`/tours/${tour.id}`);
            }}
          >
            BOOK NOW
          </button>
        </div>
      </div>
    </div>
  );
};

export default TourCard;

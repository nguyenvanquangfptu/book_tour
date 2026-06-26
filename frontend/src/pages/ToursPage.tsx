import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import TourCard from '../components/TourCard';
import { useTourOptions } from '../hooks/useTourOptions';
import { useTours } from '../hooks/useTours';
import { useTranslation } from 'react-i18next';
import '../styles/pages.css';
import '../styles/toursPage.css';
import '../styles/home.css';

const ToursPage: React.FC = () => {
  const { t } = useTranslation();
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const initialDest = searchParams.get('dest') || '';
  const initialKeyword = searchParams.get('keyword') || '';
  const initialDuration = searchParams.get('durationDays') ? Number(searchParams.get('durationDays')) : undefined;
  const initialGuests = searchParams.get('guests') ? Number(searchParams.get('guests')) : undefined;

  // Filter States
  const [keyword, setKeyword] = useState(initialKeyword);
  const [destination, setDestination] = useState(initialDest);
  const [destInputValue, setDestInputValue] = useState(initialDest);
  const [durationDays, setDurationDays] = useState<number | undefined>(initialDuration);
  const [guestsQuery, setGuestsQuery] = useState<number | undefined>(initialGuests);
  const [priceRange, setPriceRange] = useState(10000000); // UI max
  const [page, setPage] = useState(0);
  const [sortOption, setSortOption] = useState('id_ASC');
  
  const [showDestDropdown, setShowDestDropdown] = useState(false);
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [guests, setGuests] = useState(searchParams.get('guests') || '2');
  
  const [selectedTypes, setSelectedTypes] = useState<string[]>([]);
  const [selectedTransports, setSelectedTransports] = useState<string[]>([]);

  // 1. Fetch Tour Options
  const { data: optionsData } = useTourOptions();
  const allDestinations = optionsData?.destinations || [];

  // 2. Fetch Tours
  const [sortBy, sortDir] = sortOption.split('_');
  const { data: toursResponse, isLoading: loading } = useTours({
    page,
    size: 9,
    keyword,
    destination,
    durationDays,
    guests: guestsQuery,
    tourType: selectedTypes.join(','),
    transport: selectedTransports.join(','),
    minPrice: 0,
    maxPrice: priceRange,
    sortBy,
    sortDir,
  });

  const tours = toursResponse?.content || [];
  const totalPages = toursResponse?.totalPages || 0;

  // Handle URL changes from Home page search or Navbar search
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const kw = params.get('keyword');
    const dest = params.get('dest');
    const dur = params.get('durationDays');
    const gst = params.get('guests');

    let updated = false;
    
    if (kw !== null && kw !== keyword) {
      setKeyword(kw);
      updated = true;
    }
    
    if (dest !== null && dest !== destination) {
      setDestination(dest);
      setDestInputValue(dest);
      updated = true;
    }
    
    if (dur !== null && Number(dur) !== durationDays) {
      setDurationDays(Number(dur));
      updated = true;
    }
    
    if (gst !== null && Number(gst) !== guestsQuery) {
      setGuestsQuery(Number(gst));
      setGuests(gst);
      updated = true;
    }
    
    if (updated) {
      setPage(0);
    }
  }, [location.search]);

  // Auto-update search when dates or guests change
  useEffect(() => {
    let newDuration: number | undefined = undefined;
    if (checkIn && checkOut) {
      const diffTime = new Date(checkOut).getTime() - new Date(checkIn).getTime();
      if (diffTime >= 0) {
        newDuration = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
      }
    }
    
    setDurationDays(newDuration);
    setGuestsQuery(Number(guests) || undefined);
    setPage(0);
  }, [checkIn, checkOut, guests]);


  const clearFilters = () => {
    setKeyword('');
    setDestination('');
    setDestInputValue('');
    setCheckIn('');
    setCheckOut('');
    setGuests('2');
    setDurationDays(undefined);
    setGuestsQuery(undefined);
    setPriceRange(10000000);
    setSortOption('id_ASC');
    setSelectedTypes([]);
    setSelectedTransports([]);
    setPage(0);
  };



  return (
    <div className="page-wrapper" style={{ background: 'var(--bg-alt)', minHeight: '100vh', paddingBottom: '80px' }}>
      {/* Simple Page Header with Search */}
      <div className="page-header" style={{ height: '400px', backgroundImage: "url('https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=1920&q=80')" }}>
        <div className="page-overlay"></div>
        <div className="hero-content animate-fade-up" style={{ width: '100%', maxWidth: '900px', margin: '0 auto', position: 'relative', zIndex: 2 }}>
          <h1 className="hero-title" style={{ fontSize: '2.5rem', marginBottom: '30px' }}>{t('toursPage.title')}</h1>
          
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
                            onMouseDown={() => { setDestInputValue(dest); setDestination(dest); setPage(0); }}
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
              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                <button className="hero-search-btn-circular" onClick={() => {
                  setDestination(destInputValue);
                  setPage(0);
                }}>
                  <span style={{color: 'white'}}>🔍</span>
                </button>
                <button 
                  onClick={clearFilters}
                  style={{ width: '40px', height: '40px', borderRadius: '50%', background: '#f1f5f9', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.2rem', color: '#64748b', transition: 'background 0.2s' }}
                  onMouseEnter={(e) => e.currentTarget.style.background = '#e2e8f0'}
                  onMouseLeave={(e) => e.currentTarget.style.background = '#f1f5f9'}
                  title="Clear Filters"
                >
                  ✖
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="container" style={{ marginTop: '40px' }}>
        <div className="tours-layout" style={{ display: 'block' }}>
          {/* Main Content (Full Width) */}
          <div className="tours-content">
            <div className="tours-content-header animate-fade-up">
              <div className="tours-count">
                {t('toursPage.showingResults', { count: tours.length })} {destination ? t('toursPage.inDestination', { dest: destination }) : ''}
              </div>
              <div className="tours-sort">
                <select value={sortOption} onChange={(e) => { setSortOption(e.target.value); setPage(0); }}>
                  <option value="id_ASC">{t('toursPage.sortByRecommended')}</option>
                  <option value="price_ASC">{t('toursPage.priceLowToHigh')}</option>
                  <option value="price_DESC">{t('toursPage.priceHighToLow')}</option>
                </select>
              </div>
            </div>

            {loading ? (
              <div className="tours-grid">
                {[1, 2, 3, 4, 5, 6].map(n => (
                  <div key={n} style={{ height: '350px', background: 'white', borderRadius: '16px', animation: 'pulse 1.5s infinite' }}></div>
                ))}
              </div>
            ) : tours.length > 0 ? (
              <>
                <div className="tours-grid">
                  {tours.map(tour => (
                    <TourCard key={tour.id} tour={tour} />
                  ))}
                </div>
                
                {/* Pagination Controls */}
                {totalPages > 1 && (
                  <div style={{ display: 'flex', justifyContent: 'center', gap: '10px', marginTop: '60px' }}>
                    <button 
                      onClick={() => { setPage(p => Math.max(0, p - 1)); window.scrollTo({ top: 300, behavior: 'smooth' }); }}
                      disabled={page === 0}
                      className="btn btn-outline"
                      style={{ opacity: page === 0 ? 0.5 : 1, cursor: page === 0 ? 'not-allowed' : 'pointer' }}
                    >
                      {t('toursPage.previous')}
                    </button>
                    <div style={{ display: 'flex', alignItems: 'center', fontWeight: 'bold', fontSize: '1.1rem', margin: '0 10px' }}>
                      {page + 1} / {totalPages}
                    </div>
                    <button 
                      onClick={() => { setPage(p => Math.min(totalPages - 1, p + 1)); window.scrollTo({ top: 300, behavior: 'smooth' }); }}
                      disabled={page >= totalPages - 1}
                      className="btn btn-outline"
                      style={{ opacity: page >= totalPages - 1 ? 0.5 : 1, cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer' }}
                    >
                      {t('toursPage.next')}
                    </button>
                  </div>
                )}
              </>
            ) : (
              <div style={{ textAlign: 'center', padding: '80px 0', background: 'white', borderRadius: '16px', boxShadow: 'var(--shadow-sm)' }}>
                <h3 style={{ color: 'var(--text-primary)', fontSize: '1.5rem', marginBottom: '16px' }}>
                  {t('toursPage.noToursFound')}
                </h3>
                <button className="btn btn-primary" onClick={clearFilters}>
                  {t('toursPage.clearAll')}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ToursPage;

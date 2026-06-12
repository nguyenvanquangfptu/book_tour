import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import TourCard from '../components/TourCard';
import { TourService } from '../services/TourService';
import '../styles/pages.css';
import '../styles/toursPage.css';

const ToursPage: React.FC = () => {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const initialDest = searchParams.get('dest') || '';
  const initialKeyword = searchParams.get('keyword') || '';

  const [tours, setTours] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Filter States
  const [keyword, setKeyword] = useState(initialKeyword);
  const [destination, setDestination] = useState(initialDest);
  const [priceRange, setPriceRange] = useState(10000000); // UI max
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [sortOption, setSortOption] = useState('id_ASC');
  
  const [selectedTypes, setSelectedTypes] = useState<string[]>([]);
  const [selectedTransports, setSelectedTransports] = useState<string[]>([]);

  const fetchTours = async () => {
    try {
      setLoading(true);
      const [sortBy, sortDir] = sortOption.split('_');
      // Fetch tours with all filters
      const data = await TourService.getTours(page, 9, keyword, destination, '', '', priceRange, sortBy, sortDir, selectedTypes, selectedTransports);
      if (data && data.content) {
        setTours(data.content);
        setTotalPages(data.totalPages || 0);
      } else {
        setTours([]);
        setTotalPages(0);
      }
    } catch (error) {
      console.error('Failed to fetch tours', error);
      setTours([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTours();
  }, [page, destination, sortOption]);

  // Handle URL changes from Navbar search
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const kw = params.get('keyword');
    if (kw !== null) {
      setKeyword(kw);
      setPage(0);
      // Let the next render handle the fetch or fetch directly here
    }
  }, [location.search]);

  // To ensure fetching happens when keyword updates from URL
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('keyword') !== null) {
      fetchTours();
    }
  }, [keyword]);

  const handleDestinationChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setDestination(e.target.value);
    setPage(0);
  };

  const clearFilters = () => {
    setKeyword('');
    setDestination('');
    setPriceRange(10000000);
    setSortOption('id_ASC');
    setSelectedTypes([]);
    setSelectedTransports([]);
    setPage(0);
    // Note: useEffect will trigger fetchTours due to page=0, but we also manually fetch to be safe
    fetchTours();
  };

  const handleTypeToggle = (type: string) => {
    setSelectedTypes(prev => 
      prev.includes(type) ? prev.filter(t => t !== type) : [...prev, type]
    );
  };

  const handleTransportToggle = (transport: string) => {
    setSelectedTransports(prev => 
      prev.includes(transport) ? prev.filter(t => t !== transport) : [...prev, transport]
    );
  };

  return (
    <div className="page-wrapper" style={{ background: 'var(--bg-alt)', minHeight: '100vh', paddingBottom: '80px' }}>
      {/* Simple Page Header */}
      <div className="page-header" style={{ height: '30vh', minHeight: '250px', backgroundImage: "url('https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=1920&q=80')" }}>
        <div className="page-overlay"></div>
        <h1 className="page-title animate-fade-up" style={{ fontSize: '2.5rem' }}>Khám Phá Các Chuyến Đi</h1>
      </div>

      <div className="container" style={{ marginTop: '40px' }}>
        <div className="tours-layout">
          {/* Filter Sidebar */}
          <aside className="filter-sidebar animate-fade-up">
            <div className="filter-header">
              <h3>Filter Search</h3>
              <span className="clear-filter" onClick={clearFilters}>Clear All</span>
            </div>

            <div className="filter-group">
              <div className="filter-group-title">Search</div>
              <input 
                type="text" 
                className="filter-input" 
                placeholder="Search by name..." 
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && fetchTours()}
              />
            </div>

            <div className="filter-group">
              <div className="filter-group-title">Destination</div>
              <select className="filter-input" value={destination} onChange={handleDestinationChange}>
                <option value="">All Destinations</option>
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

            <div className="filter-group">
              <div className="filter-group-title">Price Range (VND)</div>
              <input 
                type="range" 
                min="0" max="10000000" step="500000"
                value={priceRange} 
                onChange={(e) => setPriceRange(Number(e.target.value))}
                style={{ width: '100%', accentColor: 'var(--primary)' }}
              />
              <div className="price-range-labels">
                <span>0đ</span>
                <span style={{ fontWeight: 600, color: 'var(--primary)' }}>
                  {new Intl.NumberFormat('vi-VN').format(priceRange)}đ
                </span>
              </div>
            </div>

            <div className="filter-group">
              <div className="filter-group-title">Tour Type</div>
              <div className="checkbox-list">
                <label className="checkbox-item">
                  <input type="checkbox" checked={selectedTypes.includes('Beach')} onChange={() => handleTypeToggle('Beach')} /> Beach
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" checked={selectedTypes.includes('Mountain')} onChange={() => handleTypeToggle('Mountain')} /> Mountain
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" checked={selectedTypes.includes('Adventure')} onChange={() => handleTypeToggle('Adventure')} /> Adventure
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" checked={selectedTypes.includes('Cultural')} onChange={() => handleTypeToggle('Cultural')} /> Cultural
                </label>
              </div>
            </div>

            <div className="filter-group">
              <div className="filter-group-title">Transport</div>
              <div className="checkbox-list">
                <label className="checkbox-item">
                  <input type="checkbox" checked={selectedTransports.includes('Flight')} onChange={() => handleTransportToggle('Flight')} /> Flight
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" checked={selectedTransports.includes('Bus')} onChange={() => handleTransportToggle('Bus')} /> Bus
                </label>
                <label className="checkbox-item">
                  <input type="checkbox" checked={selectedTransports.includes('Train')} onChange={() => handleTransportToggle('Train')} /> Train
                </label>
              </div>
            </div>
            
            <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => { setPage(0); fetchTours(); }}>
              Apply Filters
            </button>
          </aside>

          {/* Main Content */}
          <div className="tours-content">
            <div className="tours-content-header animate-fade-up">
              <div className="tours-count">
                Showing {tours.length} results {destination ? `in ${destination}` : ''}
              </div>
              <div className="tours-sort">
                <select value={sortOption} onChange={(e) => { setSortOption(e.target.value); setPage(0); }}>
                  <option value="id_ASC">Sort by: Recommended</option>
                  <option value="price_ASC">Price: Low to High</option>
                  <option value="price_DESC">Price: High to Low</option>
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
                      Previous
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
                      Next
                    </button>
                  </div>
                )}
              </>
            ) : (
              <div style={{ textAlign: 'center', padding: '80px 0', background: 'white', borderRadius: '16px', boxShadow: 'var(--shadow-sm)' }}>
                <h3 style={{ color: 'var(--text-primary)', fontSize: '1.5rem', marginBottom: '16px' }}>
                  No tours found matching your criteria.
                </h3>
                <button className="btn btn-primary" onClick={clearFilters}>
                  Clear Filters
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

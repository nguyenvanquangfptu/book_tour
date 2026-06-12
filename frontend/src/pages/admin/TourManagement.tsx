import React, { useState, useEffect } from 'react';
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa';
import { TourService } from '../../services/TourService';
import api from '../../api/axiosConfig';

const TourManagement: React.FC = () => {
  const [tours, setTours] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingTour, setEditingTour] = useState<any>(null);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: 0,
    duration: '',
    destination: '',
    imageUrl: '',
    images: [] as string[]
  });
  const [uploading, setUploading] = useState(false);

  const fetchTours = async () => {
    try {
      const data = await TourService.getTours(0, 100);
      if (data && data.content) {
        setTours(data.content);
      }
    } catch (error) {
      console.error('Failed to fetch tours', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTours();
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleAddNew = () => {
    setEditingTour(null);
    setFormData({ title: '', description: '', price: 0, duration: '', destination: '', imageUrl: '', images: [] });
    setShowModal(true);
  };

  const handleEdit = (tour: any) => {
    setEditingTour(tour);
    setFormData({
      title: tour.title,
      description: tour.description || '',
      price: tour.price,
      duration: tour.duration || '',
      destination: tour.destination || '',
      imageUrl: tour.imageUrl || '',
      images: tour.images || []
    });
    setShowModal(true);
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    
    setUploading(true);
    const uploadData = new FormData();
    Array.from(e.target.files).forEach(file => {
      uploadData.append('files', file);
    });

    try {
      const response = await api.post('/upload/multiple', uploadData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      // Giả sử server trả về list URLs trong response.data
      const urls = Array.isArray(response.data) ? response.data : (response.data.data || []);
      setFormData(prev => ({
        ...prev,
        images: [...prev.images, ...urls]
      }));
    } catch (error) {
      console.error('Lỗi khi tải ảnh lên:', error);
      alert('Tải ảnh thất bại. Vui lòng kiểm tra lại cấu hình Cloudinary trên server.');
    } finally {
      setUploading(false);
    }
  };

  const removeImage = (index: number) => {
    const newImages = [...formData.images];
    newImages.splice(index, 1);
    setFormData(prev => ({ ...prev, images: newImages }));
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa Tour này?')) {
      try {
        await TourService.deleteTour(id);
        fetchTours();
      } catch (error) {
        console.error('Lỗi khi xóa', error);
        alert('Xóa thất bại');
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingTour) {
        await TourService.updateTour(editingTour.id, formData);
      } else {
        await TourService.createTour(formData);
      }
      setShowModal(false);
      fetchTours();
    } catch (error) {
      console.error('Lỗi khi lưu Tour', error);
      alert('Lưu thất bại. Kiểm tra dữ liệu nhập.');
    }
  };

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h2>Quản lý Tour Du Lịch</h2>
        <button className="btn btn-primary" onClick={handleAddNew}>
          <FaPlus /> Thêm Tour Mới
        </button>
      </div>

      {loading ? (
        <p>Đang tải dữ liệu...</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Hình ảnh</th>
              <th>Tên Tour</th>
              <th>Giá</th>
              <th>Thời gian</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {tours.map(tour => (
              <tr key={tour.id}>
                <td>{tour.id}</td>
                <td>
                  <img 
                    src={tour.imageUrl || 'https://images.unsplash.com/photo-1504280390467-33923018e6d0?auto=format&fit=crop&w=100&q=80'} 
                    alt="Tour" 
                    style={{ width: '60px', height: '40px', objectFit: 'cover', borderRadius: '4px' }}
                    onError={(e) => {
                      const target = e.target as HTMLImageElement;
                      target.onerror = null;
                      target.src = 'https://images.unsplash.com/photo-1504280390467-33923018e6d0?auto=format&fit=crop&w=100&q=80';
                    }}
                  />
                </td>
                <td>{tour.title}</td>
                <td>{tour.price.toLocaleString()} VNĐ</td>
                <td>{tour.duration || 'N/A'}</td>
                <td>
                  <div className="action-btns">
                    <button className="btn-edit" onClick={() => handleEdit(tour)}><FaEdit /> Sửa</button>
                    <button className="btn-delete" onClick={() => handleDelete(tour.id)}><FaTrash /> Xóa</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Modal Thêm/Sửa */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>{editingTour ? 'Sửa thông tin Tour' : 'Thêm Tour Mới'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="input-group" style={{ marginBottom: '16px' }}>
                <label>Tên Tour</label>
                <input type="text" name="title" className="input-field" value={formData.title} onChange={handleInputChange} required />
              </div>
              <div className="input-group" style={{ marginBottom: '16px' }}>
                <label>Giá (VNĐ)</label>
                <input type="number" name="price" className="input-field" value={formData.price} onChange={handleInputChange} required />
              </div>
              <div className="input-group" style={{ marginBottom: '16px' }}>
                <label>Thời gian (VD: 3 Ngày 2 Đêm)</label>
                <input type="text" name="duration" className="input-field" value={formData.duration} onChange={handleInputChange} />
              </div>
              <div className="input-group" style={{ marginBottom: '16px' }}>
                <label>Mô tả chi tiết</label>
                <textarea name="description" className="input-field" rows={4} value={formData.description} onChange={handleInputChange}></textarea>
              </div>
              <div className="input-group" style={{ marginBottom: '16px' }}>
                <label>Ảnh bìa chính (URL)</label>
                <input type="url" name="imageUrl" className="input-field" value={formData.imageUrl} onChange={handleInputChange} />
              </div>
              <div className="input-group" style={{ marginBottom: '16px' }}>
                <label>Thư viện ảnh Tour (Tải lên nhiều ảnh)</label>
                <input type="file" multiple accept="image/*" className="input-field" onChange={handleFileChange} disabled={uploading} />
                {uploading && <p style={{fontSize: '0.9em', color: '#007bff', marginTop: '5px'}}>Đang tải ảnh lên Cloudinary...</p>}
                
                {formData.images.length > 0 && (
                  <div style={{ display: 'flex', gap: '10px', marginTop: '10px', flexWrap: 'wrap' }}>
                    {formData.images.map((img, idx) => (
                      <div key={idx} style={{ position: 'relative' }}>
                        <img src={img} alt={`tour-img-${idx}`} style={{ width: '80px', height: '60px', objectFit: 'cover', borderRadius: '4px' }} />
                        <button 
                          type="button" 
                          onClick={() => removeImage(idx)}
                          style={{ position: 'absolute', top: '-5px', right: '-5px', background: 'red', color: 'white', border: 'none', borderRadius: '50%', width: '20px', height: '20px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '12px' }}
                        >
                          &times;
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary">Lưu</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default TourManagement;

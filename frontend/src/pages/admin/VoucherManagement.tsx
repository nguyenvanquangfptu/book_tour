import React, { useState, useEffect } from 'react';
import { FaPlus, FaEdit, FaTrash } from 'react-icons/fa';
import { VoucherService } from '../../services/VoucherService';

const VoucherManagement: React.FC = () => {
  const [vouchers, setVouchers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState<any>(null);

  const [formData, setFormData] = useState({
    code: '',
    discountType: 'fixed', // 'fixed' or 'percentage'
    discountAmount: 0,
    discountPercentage: 0,
    maxDiscount: 0,
    minOrderValue: 0,
    validFrom: '',
    validUntil: '',
    usageLimit: 100,
    isActive: true
  });

  const fetchVouchers = async () => {
    try {
      const data = await VoucherService.getVouchers();
      if (data && data.data) {
        setVouchers(data.data);
      }
    } catch (error) {
      console.error('Failed to fetch vouchers', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVouchers();
  }, []);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    let parsedValue: any = value;
    if (type === 'number') {
      parsedValue = parseFloat(value);
    } else if (type === 'checkbox') {
      parsedValue = (e.target as HTMLInputElement).checked;
    }
    setFormData(prev => ({ ...prev, [name]: parsedValue }));
  };

  const handleAddNew = () => {
    setEditingVoucher(null);
    setFormData({
      code: '', discountType: 'fixed', discountAmount: 0, discountPercentage: 0,
      maxDiscount: 0, minOrderValue: 0, validFrom: '', validUntil: '', usageLimit: 100, isActive: true
    });
    setShowModal(true);
  };

  const handleEdit = (v: any) => {
    setEditingVoucher(v);
    setFormData({
      code: v.code,
      discountType: v.discountAmount ? 'fixed' : 'percentage',
      discountAmount: v.discountAmount || 0,
      discountPercentage: v.discountPercentage || 0,
      maxDiscount: v.maxDiscount || 0,
      minOrderValue: v.minOrderValue || 0,
      validFrom: v.validFrom ? v.validFrom.split('.')[0] : '', // format datetime-local
      validUntil: v.validUntil ? v.validUntil.split('.')[0] : '',
      usageLimit: v.usageLimit || 100,
      isActive: v.isActive
    });
    setShowModal(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa Voucher này?')) {
      try {
        await VoucherService.deleteVoucher(id);
        fetchVouchers();
      } catch (error) {
        console.error('Lỗi khi xóa voucher', error);
        alert('Xóa thất bại');
      }
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const submitData = { ...formData };
      if (submitData.discountType === 'fixed') {
        submitData.discountPercentage = 0;
      } else {
        submitData.discountAmount = 0;
      }
      
      if (editingVoucher) {
        await VoucherService.updateVoucher(editingVoucher.id, submitData);
      } else {
        await VoucherService.createVoucher(submitData);
      }
      setShowModal(false);
      fetchVouchers();
    } catch (error: any) {
      console.error('Lỗi khi lưu Voucher', error);
      alert(error.response?.data?.message || 'Lưu thất bại. Kiểm tra mã Voucher đã tồn tại chưa.');
    }
  };

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h2>Quản lý Mã Giảm Giá (Vouchers)</h2>
        <button className="btn btn-primary" onClick={handleAddNew}>
          <FaPlus /> Thêm Voucher Mới
        </button>
      </div>

      {loading ? (
        <p>Đang tải dữ liệu...</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Mã Voucher</th>
              <th>Mức giảm</th>
              <th>Đơn tối thiểu</th>
              <th>Đã dùng / Giới hạn</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {vouchers.map(v => (
              <tr key={v.id}>
                <td>{v.id}</td>
                <td><strong>{v.code}</strong></td>
                <td>{v.discountAmount ? `${v.discountAmount.toLocaleString()} đ` : `${v.discountPercentage}% (Tối đa ${v.maxDiscount}đ)`}</td>
                <td>{v.minOrderValue ? `${v.minOrderValue.toLocaleString()} đ` : '0 đ'}</td>
                <td>{v.usedCount} / {v.usageLimit}</td>
                <td>
                  <span className={`badge ${v.isActive ? 'badge-success' : 'badge-danger'}`} style={{ padding: '4px 8px', borderRadius: '12px', background: v.isActive ? '#dcfce7' : '#fee2e2', color: v.isActive ? '#166534' : '#991b1b', fontSize: '0.8rem' }}>
                    {v.isActive ? 'Hoạt động' : 'Đã khóa'}
                  </span>
                </td>
                <td>
                  <div className="action-btns">
                    <button className="btn-edit" onClick={() => handleEdit(v)}><FaEdit /> Sửa</button>
                    <button className="btn-delete" onClick={() => handleDelete(v.id)}><FaTrash /> Xóa</button>
                  </div>
                </td>
              </tr>
            ))}
            {vouchers.length === 0 && (
              <tr>
                <td colSpan={7} style={{ textAlign: 'center' }}>Chưa có mã giảm giá nào.</td>
              </tr>
            )}
          </tbody>
        </table>
      )}

      {/* Modal Thêm/Sửa */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>{editingVoucher ? 'Sửa Voucher' : 'Thêm Voucher Mới'}</h3>
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="input-group" style={{ marginBottom: '16px' }}>
                  <label>Mã Voucher (Code)</label>
                  <input type="text" name="code" className="input-field" value={formData.code} onChange={handleInputChange} style={{ textTransform: 'uppercase' }} required />
                </div>
                <div className="input-group" style={{ marginBottom: '16px' }}>
                  <label>Loại giảm giá</label>
                  <select name="discountType" className="input-field" value={formData.discountType} onChange={handleInputChange}>
                    <option value="fixed">Giảm số tiền cố định</option>
                    <option value="percentage">Giảm theo phần trăm</option>
                  </select>
                </div>
              </div>

              {formData.discountType === 'fixed' ? (
                <div className="input-group" style={{ marginBottom: '16px' }}>
                  <label>Số tiền giảm (VNĐ)</label>
                  <input type="number" name="discountAmount" className="input-field" value={formData.discountAmount} onChange={handleInputChange} required />
                </div>
              ) : (
                <div className="form-row">
                  <div className="input-group" style={{ marginBottom: '16px' }}>
                    <label>Phần trăm giảm (%)</label>
                    <input type="number" name="discountPercentage" className="input-field" value={formData.discountPercentage} onChange={handleInputChange} min="1" max="100" required />
                  </div>
                  <div className="input-group" style={{ marginBottom: '16px' }}>
                    <label>Giảm tối đa (VNĐ)</label>
                    <input type="number" name="maxDiscount" className="input-field" value={formData.maxDiscount} onChange={handleInputChange} required />
                  </div>
                </div>
              )}

              <div className="input-group" style={{ marginBottom: '16px' }}>
                <label>Giá trị đơn hàng tối thiểu (VNĐ)</label>
                <input type="number" name="minOrderValue" className="input-field" value={formData.minOrderValue} onChange={handleInputChange} />
              </div>

              <div className="form-row">
                <div className="input-group" style={{ marginBottom: '16px' }}>
                  <label>Có hiệu lực từ</label>
                  <input type="datetime-local" name="validFrom" className="input-field" value={formData.validFrom} onChange={handleInputChange} required />
                </div>
                <div className="input-group" style={{ marginBottom: '16px' }}>
                  <label>Hết hạn lúc</label>
                  <input type="datetime-local" name="validUntil" className="input-field" value={formData.validUntil} onChange={handleInputChange} required />
                </div>
              </div>

              <div className="form-row" style={{ alignItems: 'center' }}>
                <div className="input-group" style={{ marginBottom: '16px', flex: 1 }}>
                  <label>Số lượng giới hạn (Lượt dùng)</label>
                  <input type="number" name="usageLimit" className="input-field" value={formData.usageLimit} onChange={handleInputChange} required />
                </div>
                <div className="input-group" style={{ marginBottom: '16px', flex: 1, paddingLeft: '16px' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                    <input type="checkbox" name="isActive" checked={formData.isActive} onChange={handleInputChange} style={{ width: '18px', height: '18px' }} />
                    <strong>Kích hoạt Voucher</strong>
                  </label>
                </div>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-outline" onClick={() => setShowModal(false)}>Hủy</button>
                <button type="submit" className="btn btn-primary">Lưu Voucher</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default VoucherManagement;

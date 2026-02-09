let currentReservationId = null;

function openRefundModal(reservationId) {
    currentReservationId = reservationId;

    // API 호출하여 환불 정보 조회
    fetch(`/api/reservations/${reservationId}/refund-calculation`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(data => {
            if (data.code === '200') {
                const info = data.data;
                document.getElementById('policyName').textContent = info.name;
                document.getElementById('totalAmount').textContent = formatCurrency(info.totalAmount);
                document.getElementById('refundRate').textContent = info.refundRate + '%';
                document.getElementById('refundAmount').textContent = formatCurrency(info.refundAmount);

                // 모달 표시
                document.getElementById('refundModal').style.display = 'flex';
            } else {
                alert('환불 정보를 불러오는데 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        });
}

function closeRefundModal() {
    document.getElementById('refundModal').style.display = 'none';
    currentReservationId = null;
}

function confirmCancel() {
    if (currentReservationId) {
        document.getElementById('cancelForm-' + currentReservationId).submit();
    }
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('ko-KR').format(amount) + '원';
}

// 모달 바깥 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('refundModal');
    if (event.target == modal) {
        closeRefundModal();
    }
}

let currentCancelId = null;

function openRefundModal(reservationId) {
    currentCancelId = reservationId;

    // 1. 환불 정보 조회 API 호출 (경로 수정됨)
    fetch(`/api/reservations/${reservationId}/refund-calculation`)
        .then(response => {
            if (!response.ok) throw new Error("환불 정보 조회 실패");
            return response.json();
        })
        .then(apiResponse => {
            // ApiResponse 구조 (status, message, data) 처리
            const data = apiResponse.data;

            // 2. 모달 데이터 채우기
            document.getElementById('policyName').textContent = data.policyName;
            document.getElementById('totalAmount').textContent = data.totalAmount.toLocaleString() + '원';
            document.getElementById('refundRate').textContent = data.refundRate + '%';
            document.getElementById('refundAmount').textContent = data.refundAmount.toLocaleString() + '원';

            // 3. 모달 표시
            document.getElementById('refundModal').style.display = 'flex';
        })
        .catch(error => {
            console.error(error);
            alert("환불 정보를 불러오는데 실패했습니다.");
        });
}

function closeRefundModal() {
    document.getElementById('refundModal').style.display = 'none';
    currentCancelId = null;
}

function confirmCancel() {
    if (!currentCancelId) return;

    // 폼 제출
    const form = document.getElementById('cancelForm-' + currentCancelId);
    if (form) {
        form.submit();
    }
    closeRefundModal();
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modal = document.getElementById('refundModal');
    if (event.target === modal) {
        closeRefundModal();
    }
}

/**
 * 방 예약 규칙 상태 변경 (비동기 PATCH)
 */
function confirmStatusChange(checkbox, ruleId) {
    const isChecked = checkbox.checked;
    const statusText = isChecked ? '활성' : '비활성';

    // 1. 컨펌창 띄우기
    if (!confirm(`해당 규칙을 ${statusText} 상태로 변경하시겠습니까?`)) {
        checkbox.checked = !isChecked; // 취소 시 스위치 원복
        return;
    }

    // Spring Security CSRF 토큰 읽기
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;

    // 2. PATCH 요청 전송
    fetch(`/admin/roomrules/${ruleId}/status?active=${isChecked}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        }
    })
        .then(async response => {
            if (response.ok) {
                // 성공: 배지 UI 업데이트
                updateBadge(ruleId, isChecked);
            } else {
                // 실패: 에러 메시지 추출
                const msg = await response.text();

                // 만약 서버에서 HTML 에러 페이지를 보냈을 경우 (이미지처럼 소스가 뜰 때)
                if (msg.includes("<!DOCTYPE html>") || msg.includes("<html>")) {
                    // 서비스 로직의 비활성화 조건에 따른 수동 메시지 처리
                    if (!isChecked) {
                        alert("최소 1개 이상의 활성 규칙이 유지되어야 하거나, 현재 룸에서 사용 중인 규칙은 비활성화할 수 없습니다.");
                    } else {
                        alert("상태 변경 중 서버 오류가 발생했습니다.");
                    }
                } else {
                    // 서버에서 보낸 정확한 에러 메시지가 있을 경우 출력
                    alert(msg || "변경에 실패했습니다.");
                }

                checkbox.checked = !isChecked; // 스위치 원복
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("네트워크 오류가 발생했습니다.");
            checkbox.checked = !isChecked; // 스위치 원복
        });
}

/**
 * 성공 시 배지(Badge) 텍스트 및 클래스 업데이트
 */
function updateBadge(ruleId, isChecked) {
    const badge = document.getElementById(`statusText-${ruleId}`);
    if (badge) {
        badge.innerText = isChecked ? '활성' : '비활성';
        // 클래스 교체로 색상 변경
        badge.className = isChecked ? 'badge badge-active' : 'badge badge-inactive';
    }
}
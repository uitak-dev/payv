/**
 * payv 프로젝트 공통 프론트엔드 스크립트
 * 기능: 네비게이션 활성화, 아코디언, 거래 유형 전환, 태그/첨부파일 관리, AJAX 폼 제출
 */
(function () {
    // 현재 페이지 키를 body의 data-page 속성에서 가져옴 (없으면 빈 문자열)
    const page = document.body.dataset.page || "";

    // 1. 하단 네비게이션 활성화 상태 관리
    document.querySelectorAll("[data-nav]").forEach((a) => {
        const key = a.getAttribute("data-nav");
        // 현재 페이지 키와 네비게이션의 키가 일치하면 활성 스타일 적용
        if (key === page) {
            a.classList.add("text-slate-900");
            a.classList.remove("text-slate-400");
            a.setAttribute("aria-current", "page"); // 스크린 리더용 접근성 설정
        } else {
            a.classList.add("text-slate-400");
            a.classList.remove("text-slate-900");
            a.removeAttribute("aria-current");
        }
    });

    // 2. "추가 옵션" 등을 위한 아코디언 (접고 펼치기)
    document.querySelectorAll("[data-accordion-trigger]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const targetId = btn.getAttribute("data-accordion-trigger");
            const panel = document.getElementById(targetId);
            if (!panel) return;
            
            const isHidden = panel.classList.contains("hidden");
            // 클래스 hidden을 토글하여 패널을 표시하거나 숨김
            panel.classList.toggle("hidden", !isHidden);
            // 접근성 속성 업데이트 (펼쳐졌는지 여부)
            btn.setAttribute("aria-expanded", String(isHidden));
        });
    });

    // 3. 거래 유형 토글 (지출/수입 vs 이체) 화면 처리
    const typeSelect = document.querySelector("[data-tx-type]");
    if (typeSelect) {
        const txFields = document.querySelector("[data-section='tx']");             // 일반 지출/수입 필드 영역
        const transferFields = document.querySelector("[data-section='transfer']"); // 이체 전용 필드 영역
        const saveBtn = document.querySelector("[data-save-btn]");                  // 저장 버튼
        const catLevel1 = document.querySelector("[name='categoryLevel1']");        // 카테고리 선택
        const transferFrom = document.querySelector("[name='fromAsset']");          // 출금 자산
        const transferTo = document.querySelector("[name='toAsset']");              // 입금 자산

        // 선택된 유형에 따라 UI와 버튼 활성화 여부를 변경하는 함수
        function applyType() {
            const v = typeSelect.value;
            const isTransfer = v === "TRANSFER";
            
            if (txFields) txFields.classList.toggle("hidden", isTransfer);
            if (transferFields) transferFields.classList.toggle("hidden", !isTransfer);

            // 클라이언트 측 최소한의 유효성 검사 (저장 버튼 활성화)
            if (saveBtn) {
                if (isTransfer) {
                    // 이체일 때: 출금/입금 자산이 모두 있고 서로 다를 때 활성화
                    const ok = (transferFrom?.value || "") !== "" && (transferTo?.value || "") !== "" && transferFrom?.value !== transferTo?.value;
                    saveBtn.disabled = !ok;
                    saveBtn.classList.toggle("opacity-50", !ok);
                } else {
                    // 일반 거래일 때: 카테고리가 선택되어 있으면 활성화
                    const ok = (catLevel1?.value || "") !== "";
                    saveBtn.disabled = !ok;
                    saveBtn.classList.toggle("opacity-50", !ok);
                }
            }
        }

        typeSelect.addEventListener("change", applyType);
        // 주요 입력값이 변경될 때마다 버튼 상태 다시 계산
        [catLevel1, transferFrom, transferTo].forEach((el) => el && el.addEventListener("change", applyType));
        applyType(); // 초기 로드 시 1회 실행
    }

    // 4. 태그 칩(Chip) 관리 (최대 3개 제한)
    const tagInput = document.querySelector("[data-tag-input]");
    const tagAddBtn = document.querySelector("[data-tag-add]");
    const tagWrap = document.querySelector("[data-tag-wrap]");
    if (tagInput && tagAddBtn && tagWrap) {
        function tagCount() { return tagWrap.querySelectorAll("[data-tag-chip]").length; }
        
        // 태그 수에 따른 입력 상태 업데이트
        function updateTagState() {
            const ok = tagCount() < 3;
            tagAddBtn.disabled = !ok;
            tagAddBtn.classList.toggle("opacity-50", !ok);
            const hint = document.querySelector("[data-tag-hint]");
            if (hint) hint.textContent = ok ? "최대 3개까지 추가할 수 있습니다." : "태그는 최대 3개까지 가능합니다.";
        }

        tagAddBtn.addEventListener("click", () => {
            const v = tagInput.value.trim();
            if (!v || tagCount() >= 3) return;

            // 태그 칩 생성
            const chip = document.createElement("button");
            chip.type = "button";
            chip.setAttribute("data-tag-chip", "1");
            chip.className = "inline-flex items-center gap-1 rounded-full border border-slate-200 px-3 py-1 text-sm text-slate-700";
            chip.innerHTML = `<span>${escapeHtml(v)}</span><span class="text-slate-400">×</span>`;
            
            // 칩 클릭 시 삭제 기능
            chip.addEventListener("click", () => { chip.remove(); updateTagState(); });
            
            tagWrap.appendChild(chip);
            tagInput.value = "";
            updateTagState();
        });
        updateTagState();
    }

    // 5. 첨부파일(이미지) 관리 (최대 2개 + 미리보기 및 모달)
    const fileInput = document.querySelector("[data-attach-input]");
    const previewGrid = document.querySelector("[data-attach-grid]");
    const attachHint = document.querySelector("[data-attach-hint]");
    const modal = document.querySelector("[data-modal]");
    const modalImg = document.querySelector("[data-modal-img]");
    const modalClose = document.querySelector("[data-modal-close]");

    // 이미지 클릭 시 크게 보기 (모달)
    function openModal(src) {
        if (!modal || !modalImg) return;
        modalImg.src = src;
        modal.classList.remove("hidden");
        modal.setAttribute("aria-hidden", "false");
    }
    function closeModal() {
        if (!modal) return;
        modal.classList.add("hidden");
        modal.setAttribute("aria-hidden", "true");
    }

    if (modalClose) modalClose.addEventListener("click", closeModal);
    if (modal) modal.addEventListener("click", (e) => { if (e.target === modal) closeModal(); });

    if (fileInput && previewGrid) {
        fileInput.addEventListener("change", () => {
            const files = Array.from(fileInput.files || []);
            const imageFiles = files.filter(f => f.type.startsWith("image/"));
            
            if (imageFiles.length > 2) {
                if (attachHint) attachHint.textContent = "첨부는 최대 2개(이미지)까지 가능합니다.";
                fileInput.value = "";
                return;
            }

            previewGrid.innerHTML = ""; // 기존 미리보기 초기화
            imageFiles.forEach((f) => {
                const reader = new FileReader();
                reader.onload = (ev) => {
                    const src = String(ev.target?.result || "");
                    const card = document.createElement("div");
                    card.className = "relative rounded-xl border border-slate-200 overflow-hidden";
                    card.innerHTML = `
                        <button type="button" class="block w-full" aria-label="미리보기 열기">
                          <img src="${src}" alt="첨부 이미지" class="h-28 w-full object-cover"/>
                        </button>
                        <div class="px-3 py-2 text-xs text-slate-600 truncate">${escapeHtml(f.name)}</div>
                    `;
                    card.querySelector("button")?.addEventListener("click", () => openModal(src));
                    previewGrid.appendChild(card);
                };
                reader.readAsDataURL(f); // 파일을 Base64 데이터 URL로 읽음
            });
            if (attachHint) attachHint.textContent = `이미지 ${imageFiles.length}/2`;
        });
    }

    // 6. 하이브리드 렌더링: 폼 데이터를 JSON 또는 FormData로 변환하여 AJAX 제출
    function toJsonObject(formData) {
        const obj = {};
        formData.forEach((value, key) => {
            // 동일한 키가 있으면 배열로 처리 (Checkbox 등)
            if (Object.prototype.hasOwnProperty.call(obj, key)) {
                if (!Array.isArray(obj[key])) obj[key] = [obj[key]];
                obj[key].push(value);
            } else {
                obj[key] = value;
            }
        });
        return obj;
    }

    async function submitAjaxForm(form) {
        const method = (form.dataset.method || form.method || "POST").toUpperCase();
        const headers = { "X-Requested-With": "XMLHttpRequest" }; // Spring에서 AJAX 요청임을 식별
        let body;

        // data-json="true" 설정 시 JSON으로 전송
        if (form.dataset.json === "true") {
            headers["Content-Type"] = "application/json";
            body = JSON.stringify(toJsonObject(new FormData(form)));
        } else if (method === "DELETE") {
            body = undefined; // DELETE 요청은 일반적으로 Body가 없음
        } else {
            // 파일이 포함되어 있거나 multipart 설정 시 FormData 그대로 전송
            const hasFileInput = !!form.querySelector("input[type='file']");
            const isMultipart = hasFileInput || (form.enctype || "").toLowerCase().includes("multipart/form-data");
            if (isMultipart) {
                body = new FormData(form);
                // multipart는 브라우저가 경계값(boundary)을 설정해야 하므로 Content-Type을 직접 지정하지 않음
            } else {
                // 일반 폼 전송 (urlencoded)
                body = new URLSearchParams(new FormData(form));
                headers["Content-Type"] = "application/x-www-form-urlencoded;charset=UTF-8";
            }
        }

        try {
            const res = await fetch(form.action, { method, headers, body });
            let payload = null;
            try { payload = await res.json(); } catch (e) { payload = null; }

            // 서버 응답 에러 처리
            if (!res.ok || (payload && payload.success === false)) {
                const message = payload && payload.message ? payload.message : "요청 처리 중 오류가 발생했습니다.";
                window.alert(message);
                return;
            }

            // 성공 후 리다이렉트 URL이 있으면 이동, 없으면 현재 페이지 새로고침
            if (payload && payload.redirectUrl) {
                window.location.href = payload.redirectUrl;
                return;
            }
            window.location.reload();
        } catch (error) {
            window.alert("네트워크 오류가 발생했습니다.");
        }
    }

    // data-ajax="true" 속성이 있는 모든 폼에 AJAX 제출 이벤트 바인딩
    document.querySelectorAll("form[data-ajax='true']").forEach((form) => {
        form.addEventListener("submit", (e) => {
            e.preventDefault();
            submitAjaxForm(form);
        });
    });

    // XSS 방지를 위한 HTML 이스케이프 함수
    function escapeHtml(str) {
        return String(str)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
})();
(function () {
  const page = document.body.dataset.page || "";

  // Bottom nav active state
  document.querySelectorAll("[data-nav]").forEach((a) => {
    const key = a.getAttribute("data-nav");
    if (key === page) {
      a.classList.add("text-slate-900");
      a.classList.remove("text-slate-400");
      a.setAttribute("aria-current", "page");
    } else {
      a.classList.add("text-slate-400");
      a.classList.remove("text-slate-900");
      a.removeAttribute("aria-current");
    }
  });

  // Accordion for "추가 옵션"
  document.querySelectorAll("[data-accordion-trigger]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const targetId = btn.getAttribute("data-accordion-trigger");
      const panel = document.getElementById(targetId);
      if (!panel) return;
      const isHidden = panel.classList.contains("hidden");
      panel.classList.toggle("hidden", !isHidden);
      btn.setAttribute("aria-expanded", String(isHidden));
    });
  });

  // Transaction type toggle (지출/수입 vs 이체)
  const typeSelect = document.querySelector("[data-tx-type]");
  if (typeSelect) {
    const txFields = document.querySelector("[data-section='tx']");
    const transferFields = document.querySelector("[data-section='transfer']");
    const saveBtn = document.querySelector("[data-save-btn]");
    const catLevel1 = document.querySelector("[name='categoryLevel1']");
    const transferFrom = document.querySelector("[name='fromAsset']");
    const transferTo = document.querySelector("[name='toAsset']");

    function applyType() {
      const v = typeSelect.value;
      const isTransfer = v === "TRANSFER";
      if (txFields) txFields.classList.toggle("hidden", isTransfer);
      if (transferFields) transferFields.classList.toggle("hidden", !isTransfer);

      // Minimal client validation enabling save button
      if (saveBtn) {
        if (isTransfer) {
          const ok = (transferFrom?.value || "") !== "" && (transferTo?.value || "") !== "" && transferFrom?.value !== transferTo?.value;
          saveBtn.disabled = !ok;
          saveBtn.classList.toggle("opacity-50", !ok);
        } else {
          const ok = (catLevel1?.value || "") !== "";
          saveBtn.disabled = !ok;
          saveBtn.classList.toggle("opacity-50", !ok);
        }
      }
    }

    typeSelect.addEventListener("change", applyType);
    [catLevel1, transferFrom, transferTo].forEach((el) => el && el.addEventListener("change", applyType));
    applyType();
  }

  // Tag chips max 3
  const tagInput = document.querySelector("[data-tag-input]");
  const tagAddBtn = document.querySelector("[data-tag-add]");
  const tagWrap = document.querySelector("[data-tag-wrap]");
  if (tagInput && tagAddBtn && tagWrap) {
    function tagCount() { return tagWrap.querySelectorAll("[data-tag-chip]").length; }
    function updateTagState() {
      const ok = tagCount() < 3;
      tagAddBtn.disabled = !ok;
      tagAddBtn.classList.toggle("opacity-50", !ok);
      const hint = document.querySelector("[data-tag-hint]");
      if (hint) hint.textContent = ok ? "최대 3개까지 추가할 수 있습니다." : "태그는 최대 3개까지 가능합니다.";
    }
    tagAddBtn.addEventListener("click", () => {
      const v = tagInput.value.trim();
      if (!v) return;
      if (tagCount() >= 3) return;
      const chip = document.createElement("button");
      chip.type = "button";
      chip.setAttribute("data-tag-chip", "1");
      chip.className = "inline-flex items-center gap-1 rounded-full border border-slate-200 px-3 py-1 text-sm text-slate-700";
      chip.innerHTML = `<span>${escapeHtml(v)}</span><span class="text-slate-400">×</span>`;
      chip.addEventListener("click", () => { chip.remove(); updateTagState(); });
      tagWrap.appendChild(chip);
      tagInput.value = "";
      updateTagState();
    });
    updateTagState();
  }

  // Attachments (images only) max 2 + preview
  const fileInput = document.querySelector("[data-attach-input]");
  const previewGrid = document.querySelector("[data-attach-grid]");
  const attachHint = document.querySelector("[data-attach-hint]");
  const modal = document.querySelector("[data-modal]");
  const modalImg = document.querySelector("[data-modal-img]");
  const modalClose = document.querySelector("[data-modal-close]");

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
      // Only allow images
      const imageFiles = files.filter(f => f.type.startsWith("image/"));
      if (imageFiles.length > 2) {
        if (attachHint) attachHint.textContent = "첨부는 최대 2개(이미지)까지 가능합니다.";
        fileInput.value = "";
        return;
      }
      previewGrid.innerHTML = "";
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
        reader.readAsDataURL(f);
      });
      if (attachHint) attachHint.textContent = `이미지 ${imageFiles.length}/2`;
    });
  }

  // Hybrid rendering: GET is SSR, mutations are AJAX
  function toJsonObject(formData) {
    const obj = {};
    formData.forEach((value, key) => {
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
    const headers = { "X-Requested-With": "XMLHttpRequest" };
    let body;

    if (form.dataset.json === "true") {
      headers["Content-Type"] = "application/json";
      body = JSON.stringify(toJsonObject(new FormData(form)));
    } else if (method === "DELETE") {
      body = undefined;
    } else {
      const hasFileInput = !!form.querySelector("input[type='file']");
      const isMultipart = hasFileInput || (form.enctype || "").toLowerCase().includes("multipart/form-data");
      if (isMultipart) {
        body = new FormData(form);
      } else {
        body = new URLSearchParams(new FormData(form));
        headers["Content-Type"] = "application/x-www-form-urlencoded;charset=UTF-8";
      }
    }

    const res = await fetch(form.action, {
      method,
      headers,
      body
    });

    let payload = null;
    try {
      payload = await res.json();
    } catch (e) {
      payload = null;
    }

    if (!res.ok || (payload && payload.success === false)) {
      const message = payload && payload.message ? payload.message : "요청 처리 중 오류가 발생했습니다.";
      window.alert(message);
      return;
    }

    if (payload && payload.redirectUrl) {
      window.location.href = payload.redirectUrl;
      return;
    }

    window.location.reload();
  }

  document.querySelectorAll("form[data-ajax='true']").forEach((form) => {
    form.addEventListener("submit", (e) => {
      e.preventDefault();
      submitAjaxForm(form);
    });
  });

  function escapeHtml(str) {
    return String(str)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#039;");
  }
})();

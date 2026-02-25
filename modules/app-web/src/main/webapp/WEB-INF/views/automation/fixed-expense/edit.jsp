<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>고정비 수정</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="settings" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/automation/fixed-expenses" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">뒤로</a>
            <h1 class="text-base font-semibold">고정비 수정</h1>
            <span class="w-12"></span>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <form method="post" action="${ctx}/automation/fixed-expenses/${fixedExpense.definitionId}" class="space-y-4" data-ajax="true" data-method="PUT" data-json="true">
            <section class="pv-card p-4">
                <div class="grid gap-4">
                    <label>
                        <div class="text-sm font-medium">이름</div>
                        <input name="name" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" value="${fixedExpense.name}"/>
                    </label>

                    <label>
                        <div class="text-sm font-medium">금액</div>
                        <input name="amount" type="number" min="1" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" value="${fixedExpense.amount}"/>
                    </label>

                    <label>
                        <div class="text-sm font-medium">자산</div>
                        <select name="assetId" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="">선택</option>
                            <c:forEach var="asset" items="${assets}">
                                <option value="${asset.assetId}" ${asset.assetId == fixedExpense.assetId ? 'selected' : ''}>${asset.name}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label>
                        <div class="text-sm font-medium">1단계 카테고리</div>
                        <select id="categoryLevel1" name="categoryIdLevel1" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="">선택</option>
                            <c:forEach var="root" items="${categories}">
                                <option value="${root.categoryId}" ${root.categoryId == fixedExpense.categoryIdLevel1 ? 'selected' : ''}>${root.name}</option>
                            </c:forEach>
                        </select>
                    </label>

                    <label>
                        <div class="text-sm font-medium">2단계 카테고리(선택)</div>
                        <select id="categoryLevel2" name="categoryIdLevel2" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="">선택</option>
                            <c:forEach var="root" items="${categories}">
                                <c:forEach var="child" items="${root.children}">
                                    <option value="${child.categoryId}" data-parent="${root.categoryId}" ${child.categoryId == fixedExpense.categoryIdLevel2 ? 'selected' : ''}>${root.name} / ${child.name}</option>
                                </c:forEach>
                            </c:forEach>
                        </select>
                    </label>

                    <label>
                        <div class="text-sm font-medium">실행 주기</div>
                        <select id="scheduleType" name="scheduleType" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="DAY" ${!fixedExpense.endOfMonth ? 'selected' : ''}>매월 특정일</option>
                            <option value="EOM" ${fixedExpense.endOfMonth ? 'selected' : ''}>매월 말일</option>
                        </select>
                    </label>

                    <label id="dayOfMonthWrap">
                        <div class="text-sm font-medium">실행일(1~31)</div>
                        <input id="dayOfMonth" name="dayOfMonth" type="number" min="1" max="31" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" value="${fixedExpense.dayOfMonth}"/>
                    </label>

                    <label>
                        <div class="text-sm font-medium">메모</div>
                        <textarea name="memo" rows="3" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="메모를 입력하세요">${fixedExpense.memo}</textarea>
                    </label>
                </div>
            </section>

            <button type="submit" class="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">수정</button>
        </form>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
<script>
    (function() {
        const level1 = document.getElementById('categoryLevel1');
        const level2 = document.getElementById('categoryLevel2');
        const scheduleType = document.getElementById('scheduleType');
        const dayOfMonthWrap = document.getElementById('dayOfMonthWrap');
        const dayOfMonth = document.getElementById('dayOfMonth');

        if (level1 && level2) {
            const filterLevel2 = function() {
                const rootId = level1.value;
                Array.prototype.forEach.call(level2.options, function(option) {
                    const parent = option.getAttribute('data-parent');
                    if (!parent) {
                        option.hidden = false;
                        return;
                    }
                    option.hidden = rootId !== '' && parent !== rootId;
                    if (option.hidden && option.selected) {
                        option.selected = false;
                    }
                });
            };
            level1.addEventListener('change', filterLevel2);
            filterLevel2();
        }

        if (scheduleType && dayOfMonthWrap && dayOfMonth) {
            const applyScheduleType = function() {
                const eom = scheduleType.value === 'EOM';
                dayOfMonthWrap.classList.toggle('hidden', eom);
                dayOfMonth.disabled = eom;
                dayOfMonth.required = !eom;
            };
            scheduleType.addEventListener('change', applyScheduleType);
            applyScheduleType();
        }
    })();
</script>
</body>
</html>

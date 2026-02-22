<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="selectedType" value="${mode == 'edit' ? tx.transactionType : (not empty requestedType ? requestedType : 'EXPENSE')}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>${mode == 'edit' ? '거래 수정' : '거래 추가'}</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="report" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/ledger/transactions${mode == 'edit' ? '/' : ''}${mode == 'edit' ? tx.transactionId : ''}" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">뒤로</a>
            <h1 class="text-base font-semibold">${mode == 'edit' ? '거래 수정' : '거래 추가'}</h1>
            <span class="w-12"></span>
        </div>
    </header>

    <main class="px-4 pt-4 pb-24">
        <c:if test="${not empty error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <form action="${ctx}${action}" method="post" class="space-y-4" data-ajax="true" data-method="${mode == 'edit' ? 'PUT' : 'POST'}" data-json="${mode == 'edit' ? 'true' : 'false'}">
            <section class="pv-card p-4">
                <div class="grid gap-4">
                    <label class="block">
                        <div class="text-sm font-medium">유형</div>
                        <select data-tx-type name="transactionType" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                            <option value="EXPENSE" ${selectedType == 'EXPENSE' ? 'selected' : ''}>지출</option>
                            <option value="INCOME" ${selectedType == 'INCOME' ? 'selected' : ''}>수입</option>
                            <c:if test="${mode != 'edit'}">
                                <option value="TRANSFER" ${selectedType == 'TRANSFER' ? 'selected' : ''}>이체</option>
                            </c:if>
                        </select>
                    </label>

                    <label class="block">
                        <div class="text-sm font-medium">금액</div>
                        <input name="amount" inputmode="numeric" required type="number" min="1" value="${mode == 'edit' ? tx.amount : ''}" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" placeholder="0"/>
                    </label>

                    <label class="block">
                        <div class="text-sm font-medium">날짜</div>
                        <input name="transactionDate" type="date" required value="${mode == 'edit' ? tx.transactionDate : today}" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                    </label>

                    <div data-section="tx" class="${selectedType == 'TRANSFER' ? 'hidden grid gap-4' : 'grid gap-4'}">
                        <label class="block">
                            <div class="text-sm font-medium">자산</div>
                            <select name="assetId" data-required-for="tx" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                                <option value="">선택</option>
                                <c:forEach var="asset" items="${assets}">
                                    <option value="${asset.assetId}" ${mode == 'edit' and asset.assetId == tx.assetId ? 'selected' : ''}>${asset.name}</option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="block">
                            <div class="text-sm font-medium">1단계 카테고리</div>
                            <select id="categoryLevel1" name="categoryIdLevel1" data-required-for="tx" required class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                                <option value="">선택</option>
                                <c:forEach var="root" items="${categories}">
                                    <option value="${root.categoryId}" ${mode == 'edit' and root.categoryId == tx.categoryIdLevel1 ? 'selected' : ''}>${root.name}</option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="block">
                            <div class="text-sm font-medium">2단계 카테고리(선택)</div>
                            <select id="categoryLevel2" name="categoryIdLevel2" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                                <option value="">선택</option>
                                <c:forEach var="root" items="${categories}">
                                    <c:forEach var="child" items="${root.children}">
                                        <option value="${child.categoryId}" data-parent="${root.categoryId}" ${mode == 'edit' and child.categoryId == tx.categoryIdLevel2 ? 'selected' : ''}>${root.name} / ${child.name}</option>
                                    </c:forEach>
                                </c:forEach>
                            </select>
                        </label>

                        <div>
                            <div class="text-sm font-medium">태그 (최대 3개)</div>
                            <div class="mt-2 grid grid-cols-2 gap-2">
                                <c:forEach var="tag" items="${tags}">
                                    <label class="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-sm">
                                        <input type="checkbox" name="tagIds" value="${tag.tagId}" ${selectedTagMap[tag.tagId] ? 'checked' : ''}/>
                                        <span>${tag.name}</span>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </div>

                    <div data-section="transfer" class="${selectedType == 'TRANSFER' ? 'grid gap-4' : 'hidden grid gap-4'}">
                        <label class="block">
                            <div class="text-sm font-medium">출금 자산</div>
                            <select name="fromAssetId" data-required-for="transfer" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                                <option value="">선택</option>
                                <c:forEach var="asset" items="${assets}">
                                    <option value="${asset.assetId}">${asset.name}</option>
                                </c:forEach>
                            </select>
                        </label>

                        <label class="block">
                            <div class="text-sm font-medium">입금 자산</div>
                            <select name="toAssetId" data-required-for="transfer" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm">
                                <option value="">선택</option>
                                <c:forEach var="asset" items="${assets}">
                                    <option value="${asset.assetId}">${asset.name}</option>
                                </c:forEach>
                            </select>
                            <p class="mt-1 text-xs text-slate-500">출금/입금 자산은 동일할 수 없습니다.</p>
                        </label>
                    </div>

                    <label class="block">
                        <div class="text-sm font-medium">메모</div>
                        <textarea name="memo" class="mt-2 w-full rounded-xl border border-slate-200 px-3 py-2 text-sm" rows="3" placeholder="메모를 입력하세요">${mode == 'edit' ? tx.memo : ''}</textarea>
                    </label>
                </div>
            </section>

            <button type="submit" class="w-full rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white">${submitLabel}</button>
        </form>

    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
<script>
    (function() {
        const level1 = document.getElementById('categoryLevel1');
        const level2 = document.getElementById('categoryLevel2');
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

        const tagChecks = document.querySelectorAll('input[name="tagIds"]');
        const enforceTagLimit = function() {
            let checkedCount = 0;
            Array.prototype.forEach.call(tagChecks, function(input) {
                if (input.checked) checkedCount += 1;
            });
            Array.prototype.forEach.call(tagChecks, function(input) {
                if (!input.checked) {
                    input.disabled = checkedCount >= 3;
                }
            });
        };
        Array.prototype.forEach.call(tagChecks, function(input) {
            input.addEventListener('change', enforceTagLimit);
        });
        enforceTagLimit();
    })();
</script>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>거래 상세</title>
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="stylesheet" href="${ctx}/assets/styles.css"/>
</head>
<body data-page="ledger" class="bg-slate-100 text-slate-900">
<div class="app-shell shadow-sm">
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white">
        <div class="flex items-center justify-between px-4 py-3">
            <a href="${ctx}/ledger/transactions" class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm">목록</a>
            <h1 class="text-base font-semibold">거래 상세</h1>
            <a href="${ctx}/ledger/transactions/${tx.transactionId}/edit" class="rounded-lg bg-slate-900 px-3 py-1.5 text-sm text-white">수정</a>
        </div>
    </header>

    <main class="px-4 pt-4 pv-safe-bottom">
        <c:if test="${not empty notice.created}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">거래가 생성되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.updated}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">거래가 수정되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.attachmentUploaded}">
            <p class="mb-3 rounded-xl border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">첨부 파일이 업로드되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.attachmentDeleted}">
            <p class="mb-3 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">첨부가 삭제되었습니다.</p>
        </c:if>
        <c:if test="${not empty notice.error}">
            <p class="mb-3 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">요청 처리 중 오류가 발생했습니다.</p>
        </c:if>

        <div class="space-y-4">
            <section class="pv-card p-4">
                <div class="flex items-start justify-between">
                    <div>
                        <div class="text-xs text-slate-500">${tx.transactionDate}</div>
                        <c:if test="${tx.sourceType == 'FIXED_COST_AUTO'}">
                            <div class="mt-1 inline-flex rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-700">
                                ${tx.sourceDisplayName} 자동 생성
                            </div>
                        </c:if>
                        <div class="mt-1 text-lg font-semibold">${tx.categoryNameLevel1} ${not empty tx.categoryNameLevel2 ? '·' : ''} ${tx.categoryNameLevel2}</div>
                        <div class="mt-1 text-sm text-slate-600">${tx.assetName}</div>
                    </div>
                    <div class="text-lg font-semibold ${tx.transactionType == 'EXPENSE' ? 'text-red-600' : 'text-green-600'}">
                        <c:choose>
                            <c:when test="${tx.transactionType == 'EXPENSE'}">-</c:when>
                            <c:otherwise>+</c:otherwise>
                        </c:choose>
                        <fmt:formatNumber value="${tx.amount}" pattern="#,##0"/>
                    </div>
                </div>

                <div class="mt-4 grid gap-2 text-sm">
                    <div class="flex justify-between"><span class="text-slate-500">거래유형</span><span>${tx.transactionType}</span></div>
                    <div class="flex justify-between"><span class="text-slate-500">생성방식</span><span>${tx.sourceDisplayName}</span></div>
                    <c:if test="${not empty tx.fixedCostTemplateId}">
                        <div class="flex justify-between"><span class="text-slate-500">고정비 마스터 ID</span><span>${tx.fixedCostTemplateId}</span></div>
                    </c:if>
                    <div class="flex justify-between"><span class="text-slate-500">메모</span><span>${tx.memo}</span></div>
                </div>

                <form method="post" action="${ctx}/ledger/transactions/${tx.transactionId}" class="mt-4" data-ajax="true" data-method="DELETE">
                    <button type="submit" class="w-full rounded-xl border border-red-200 px-4 py-3 text-sm font-semibold text-red-700">삭제</button>
                </form>
            </section>

            <section class="pv-card p-4">
                <div class="text-sm font-semibold">태그</div>
                <div class="mt-2 flex flex-wrap gap-2">
                    <c:forEach var="tag" items="${tx.tags}">
                        <span class="rounded-full border border-slate-200 px-3 py-1 text-sm">${tag.tagName}</span>
                    </c:forEach>
                    <c:if test="${empty tx.tags}">
                        <span class="text-sm text-slate-500">선택된 태그가 없습니다.</span>
                    </c:if>
                </div>
            </section>

            <section class="pv-card p-4">
                <div class="flex items-center justify-between">
                    <div class="text-sm font-semibold">첨부 파일</div>
                    <div class="text-xs text-slate-500">${fn:length(tx.attachments)}/2</div>
                </div>

                <c:if test="${fn:length(tx.attachments) < 2}">
                    <form class="mt-3 flex items-center gap-2"
                          method="post"
                          enctype="multipart/form-data"
                          action="${ctx}/ledger/transactions/${tx.transactionId}/attachments"
                          data-ajax="true">
                        <input type="file"
                               name="file"
                               accept="image/*"
                               required
                               class="block w-full flex-1 rounded-xl border border-slate-200 px-3 py-2 text-sm"/>
                        <button type="submit" class="rounded-xl border border-slate-200 px-3 py-2 text-sm">업로드</button>
                    </form>
                </c:if>
                <c:if test="${fn:length(tx.attachments) >= 2}">
                    <p class="mt-3 text-xs text-slate-500">첨부 파일은 최대 2개까지 업로드할 수 있습니다.</p>
                </c:if>

                <div class="mt-3 grid grid-cols-2 gap-3">
                    <c:forEach var="att" items="${tx.attachments}">
                        <c:url var="attImageUrl" value="/ledger/transactions/${tx.transactionId}/attachments/${att.attachmentId}/image"/>
                        <div class="overflow-hidden rounded-xl border border-slate-200">
                            <a href="${attImageUrl}" target="_blank" rel="noopener noreferrer" class="block">
                                <img src="${attImageUrl}" alt="${att.uploadFileName}" class="pv-thumb h-24 w-full object-cover"/>
                            </a>
                            <div class="flex items-center justify-between gap-2 px-3 py-2">
                                <div class="truncate text-xs text-slate-600">${att.uploadFileName}</div>
                                <form method="post"
                                      action="${ctx}/ledger/transactions/${tx.transactionId}/attachments/${att.attachmentId}"
                                      data-ajax="true"
                                      data-method="DELETE">
                                    <button type="submit" class="text-xs text-slate-500">삭제</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty tx.attachments}">
                        <p class="col-span-2 text-sm text-slate-500">첨부 파일이 없습니다.</p>
                    </c:if>
                </div>
            </section>
        </div>
    </main>
</div>

<%@ include file="/WEB-INF/views/common/bottom-nav.jspf" %>

<script src="${ctx}/assets/app.js"></script>
</body>
</html>

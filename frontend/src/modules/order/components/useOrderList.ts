import { FormEvent, useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { orderApi } from "../api/orderApi";
import { OrderSummary, PageResponse } from "../types";
import { errorMessage } from "./OrderUI";

export function useOrderList(staff = false) {
  const [params, setParams] = useSearchParams();
  const rawPage = Number(params.get("page") || 0);
  const page = Number.isSafeInteger(rawPage) && rawPage >= 0 ? rawPage : 0;
  const status = params.get("status") || "";
  const search = params.get("search") || "";
  const [draft, setDraft] = useState(search);
  const [data, setData] = useState<PageResponse<OrderSummary> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reload, setReload] = useState(0);
  useEffect(() => setDraft(search), [search]);
  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    const request = staff ? orderApi.getAllForStaff : orderApi.getMine;
    request({ page, size: 10, search, status: status || undefined })
      .then((value) => {
        if (active) setData(value);
      })
      .catch((err) => {
        if (active) {
          setData(null);
          setError(errorMessage(err, "Không thể tải danh sách đơn hàng."));
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [staff, page, status, search, reload]);
  function update(changes: Record<string, string>) {
    const next = new URLSearchParams(params);
    Object.entries(changes).forEach(([key, value]) =>
      value ? next.set(key, value) : next.delete(key),
    );
    setParams(next);
  }
  function submit(e: FormEvent) {
    e.preventDefault();
    update({ search: draft.trim(), page: "" });
  }
  return {
    data,
    loading,
    error,
    page,
    status,
    draft,
    setDraft,
    submit,
    retry: () => setReload((n) => n + 1),
    changeStatus: (value: string) => update({ status: value, page: "" }),
    changePage: (value: number) => update({ page: String(value) }),
  };
}

(function () {
    const payButton = document.getElementById("payButton");
    const checkoutForm = document.getElementById("checkoutForm");
    const message = document.getElementById("paymentMessage");
    const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;

    if (!payButton) {
        return;
    }

    function setMessage(text) {
        if (message) {
            message.textContent = text;
        }
    }

    function headers() {
        const requestHeaders = {
            "Content-Type": "application/x-www-form-urlencoded"
        };

        if (csrfHeader && csrfToken) {
            requestHeaders[csrfHeader] = csrfToken;
        }

        return requestHeaders;
    }

    function body(params) {
        return new URLSearchParams(params).toString();
    }

    async function post(url, params) {
        const response = await fetch(url, {
            method: "POST",
            headers: headers(),
            body: body(params)
        });

        if (!response.ok) {
            throw new Error("Request failed.");
        }

        return response.json();
    }

    payButton.addEventListener("click", async function () {
        const shippingAddress = new FormData(checkoutForm).get("shippingAddress");

        if (!shippingAddress) {
            checkoutForm.reportValidity();
            return;
        }

        payButton.disabled = true;
        setMessage("Creating Razorpay order...");

        try {
            const order = await post(payButton.dataset.createUrl, {
                shippingAddress: shippingAddress
            });

            const options = {
                key: order.key,
                amount: order.amount,
                currency: order.currency,
                name: "Payment Gateway System",
                description: "Cart checkout",
                order_id: order.orderId,
                handler: async function (response) {
                    setMessage("Verifying payment...");

                    const verification = await post(payButton.dataset.verifyUrl, {
                        razorpay_order_id: response.razorpay_order_id,
                        razorpay_payment_id: response.razorpay_payment_id,
                        razorpay_signature: response.razorpay_signature
                    });

                    window.location.href = verification.redirectUrl;
                },
                modal: {
                    ondismiss: async function () {
                        setMessage("Payment cancelled.");
                        await post(payButton.dataset.failedUrl, {
                            razorpay_order_id: order.orderId,
                            reason: "Payment window closed before completion."
                        });
                        window.location.href = "/dashboard/payment-result?status=failed&orderId=" + order.localOrderId;
                    }
                },
                theme: {
                    color: "#2563eb"
                }
            };

            const razorpay = new Razorpay(options);
            razorpay.on("payment.failed", async function (response) {
                const reason = response.error && response.error.description
                    ? response.error.description
                    : "Payment failed in Razorpay checkout.";

                const failed = await post(payButton.dataset.failedUrl, {
                    razorpay_order_id: order.orderId,
                    reason: reason
                });

                window.location.href = failed.redirectUrl;
            });

            razorpay.open();
            setMessage("Complete payment in the Razorpay window.");
        } catch (error) {
            payButton.disabled = false;
            setMessage("Unable to start payment. Check Razorpay test keys and try again.");
        }
    });
})();

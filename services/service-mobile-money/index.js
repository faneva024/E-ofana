import express from "express";

const app = express();
const PORT = process.env.MOBILE_MONEY_SERVICE_PORT || 3003;

app.use(express.json());

app.get("/", (req, res) => {
  res.json({
    status: "success",
    message: "Service Mobile Money mock E-OFANA actif",
    mode: process.env.MOBILE_MONEY_MODE || "mock"
  });
});

app.post("/paiement/confirmer", (req, res) => {
  console.log(JSON.stringify({
    service: "service-mobile-money",
    date: new Date().toISOString(),
    action: "PAYMENT_MOCK",
    body: req.body
  }));

  res.json({
    status: "success",
    message: "Paiement simulé avec succès",
    reference: `MM-${Date.now()}`
  });
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Service Mobile Money lancé sur le port ${PORT}`);
});

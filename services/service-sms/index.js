import express from "express";

const app = express();
const PORT = process.env.SMS_SERVICE_PORT || 3002;

app.use(express.json());

app.get("/", (req, res) => {
  res.json({
    status: "success",
    message: "Service SMS E-OFANA actif",
    mode: process.env.SMS_MODE || "mock"
  });
});

app.post("/sms/envoyer", (req, res) => {
  console.log(JSON.stringify({
    service: "service-sms",
    date: new Date().toISOString(),
    action: "SMS_MOCK",
    body: req.body
  }));

  res.json({
    status: "success",
    message: "SMS simulé avec succès"
  });
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Service SMS lancé sur le port ${PORT}`);
});

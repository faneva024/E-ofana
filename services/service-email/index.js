import express from "express";

const app = express();
const PORT = process.env.EMAIL_SERVICE_PORT || 3001;

app.use(express.json());

app.get("/", (req, res) => {
  res.json({
    status: "success",
    message: "Service email E-OFANA actif",
    mode: process.env.EMAIL_MODE || "mock"
  });
});

app.post("/email/envoyer", (req, res) => {
  console.log(JSON.stringify({
    service: "service-email",
    date: new Date().toISOString(),
    action: "EMAIL_MOCK",
    body: req.body
  }));

  res.json({
    status: "success",
    message: "Email simulé avec succès"
  });
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Service email lancé sur le port ${PORT}`);
});

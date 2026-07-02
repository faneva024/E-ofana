-- Description : Données de référence — 12 catégories de formations
--               Ces données sont nécessaires au fonctionnement de l'application
--               (filtres, affichage catalogue). Elles ne sont PAS des données de test.
--               Requiert : V4 (table categories)
SET search_path TO eofana;

INSERT INTO "categories" ("nom", "description", "icone", "couleur") VALUES
    ('Informatique & Tech',
     'Développement web, mobile, réseaux, cybersécurité, bureautique',
     'bi-laptop',        '#2E75B6'),

    ('Langues',
     'Anglais, Français, Chinois, Malgache des affaires et certifications',
     'bi-translate',     '#70AD47'),

    ('Gestion & Finance',
     'Management, gestion de projet, stratégie d''entreprise',
     'bi-graph-up',      '#ED7D31'),

    ('Marketing & Communication',
     'Marketing digital, réseaux sociaux, communication professionnelle',
     'bi-megaphone',     '#7030A0'),

    ('Ressources Humaines',
     'Recrutement, droit du travail, gestion des talents',
     'bi-people',        '#C55A11'),

    ('Comptabilité',
     'Comptabilité générale, fiscalité malagasy, logiciels comptables',
     'bi-calculator',    '#1F4E79'),

    ('Design & Créativité',
     'Graphisme, UI/UX, photographie, vidéo',
     'bi-palette',       '#FF0000'),

    ('Santé & Bien-être',
     'Secourisme, nutrition, gestion du stress, ergonomie',
     'bi-heart-pulse',   '#00B050'),

    ('Droit & Juridique',
     'Droit des affaires, contrats, propriété intellectuelle',
     'bi-briefcase',     '#404040'),

    ('Mécanique & Technique',
     'Maintenance industrielle, mécanique auto, électrotechnique',
     'bi-tools',         '#795548'),

    ('Agriculture & Élevage',
     'Techniques agricoles modernes, élevage, agro-transformation',
     'bi-flower1',       '#558B2F'),

    ('Autre',
     'Formations diverses non classées dans les catégories ci-dessus',
     'bi-three-dots',    '#888888');

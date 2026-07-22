-- Seed reference departments (mirror auth org units) and default email templates
-- in en/az/ru. created_by uses the well-known nil system UUID.

INSERT INTO departments (code, name, is_active) VALUES
  ('IT',   'Information Technology', TRUE),
  ('HR',   'Human Resources',        TRUE),
  ('FIN',  'Finance',                TRUE),
  ('ENG',  'Engineering Faculty',    TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO hr_templates (type, name, subject, body, created_by, is_active) VALUES
  ('approval',  'Default Approval (EN)',
   'Your application for {{vacancy}} has been approved',
   'Dear {{name}},

We are pleased to inform you that your application for the position of {{vacancy}} has been approved. Our HR team will contact you shortly regarding the onboarding process.

Best regards,
AzTU HR Department',
   '00000000-0000-0000-0000-000000000000', TRUE),

  ('rejection', 'Default Rejection (EN)',
   'Update on your application for {{vacancy}}',
   'Dear {{name}},

Thank you for applying for the position of {{vacancy}}. After careful consideration, we regret to inform you that your application was not successful.

Reason: {{reason}}

We wish you success in your future endeavours.

Best regards,
AzTU HR Department',
   '00000000-0000-0000-0000-000000000000', TRUE),

  ('onboarding_step', 'Default Onboarding (EN)',
   'Onboarding step for {{vacancy}}',
   'Dear {{name}},

This is an update regarding your onboarding for the position of {{vacancy}}. Please review the attached documents and complete the required steps.

Best regards,
AzTU HR Department',
   '00000000-0000-0000-0000-000000000000', TRUE),

  ('termination', 'Default Termination (EN)',
   'Notice of termination',
   'Dear {{name}},

This letter is to formally notify you that your employment will be terminated.

Reason: {{reason}}

We thank you for your service.

Best regards,
AzTU HR Department',
   '00000000-0000-0000-0000-000000000000', TRUE),

  ('approval',  'Standart Təsdiq (AZ)',
   '{{vacancy}} müraciətiniz təsdiqləndi',
   'Hörmətli {{name}},

{{vacancy}} vəzifəsi üzrə müraciətinizin təsdiqləndiyini bildirməkdən məmnunuq. HR komandamız işə qəbul prosesi ilə bağlı tezliklə sizinlə əlaqə saxlayacaq.

Hörmətlə,
AzTU HR Departamenti',
   '00000000-0000-0000-0000-000000000000', TRUE),

  ('rejection', 'Standart İmtina (AZ)',
   '{{vacancy}} müraciətiniz barədə',
   'Hörmətli {{name}},

{{vacancy}} vəzifəsinə müraciət etdiyiniz üçün təşəkkür edirik. Təəssüf ki, müraciətiniz uğurlu olmadı.

Səbəb: {{reason}}

Gələcək fəaliyyətinizdə uğurlar arzulayırıq.

Hörmətlə,
AzTU HR Departamenti',
   '00000000-0000-0000-0000-000000000000', TRUE),

  ('approval',  'Стандартное одобрение (RU)',
   'Ваша заявка на {{vacancy}} одобрена',
   'Уважаемый(ая) {{name}},

Рады сообщить, что ваша заявка на должность {{vacancy}} одобрена. Наша HR-команда свяжется с вами в ближайшее время по поводу оформления.

С уважением,
HR-департамент AzTU',
   '00000000-0000-0000-0000-000000000000', TRUE),

  ('rejection', 'Стандартный отказ (RU)',
   'Обновление по вашей заявке на {{vacancy}}',
   'Уважаемый(ая) {{name}},

Благодарим за отклик на должность {{vacancy}}. К сожалению, ваша заявка не была одобрена.

Причина: {{reason}}

Желаем успехов.

С уважением,
HR-департамент AzTU',
   '00000000-0000-0000-0000-000000000000', TRUE);

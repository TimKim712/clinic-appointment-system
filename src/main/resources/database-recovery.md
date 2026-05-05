# Check application health
curl http://localhost:8080/health

# Expected output if database is down:
# {"status":"DOWN","database":{"status":"DOWN",...}}

# Check if database is running
systemctl status postgresql

# Expected output if down:
# ● postgresql.service - PostgreSQL Database Server
#    Loaded: loaded (/usr/lib/systemd/system/postgresql.service)
#    Active: inactive (dead)

# Restart PostgreSQL
sudo systemctl restart postgresql

# Wait a few seconds
sleep 5

# Verify it's running
systemctl status postgresql

# Expected output if successful:
# ● postgresql.service - PostgreSQL Database Server
#    Loaded: loaded
#    Active: active (running) since Mon 2026-04-27 14:31:00 PDT

# Try to connect directly
psql -h localhost -U clinic_user -d clinic_db -c "SELECT 1"

# Expected output if successful:
#  ?column? 
# ----------
#         1
# (1 row)
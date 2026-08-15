const express = require('express');
const mysql = require('mysql2/promise');
const app = express();

app.use(express.json());

// Configure Database connection
const dbConfig = {
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '123456',
    database: process.env.DB_NAME || 'tetris_dungeon'
};

async function initDB() {
    const conn = await mysql.createConnection({ ...dbConfig, database: undefined });
    await conn.query(`CREATE DATABASE IF NOT EXISTS \`${dbConfig.database}\``);
    await conn.end();
    
    const pool = mysql.createPool(dbConfig);
    await pool.query(`
        CREATE TABLE IF NOT EXISTS highscores (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(50) NOT NULL,
            score INT NOT NULL,
            level INT NOT NULL,
            played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    `);
    return pool;
}

let pool;
initDB().then(p => { pool = p; console.log("Database connected!"); }).catch(console.error);

// GET Top Scores (Returns CSV format for easy Java parsing without Gson)
app.get('/api/scores', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT name, score, level FROM highscores ORDER BY score DESC LIMIT 10');
        const csv = rows.map(r => `${r.name},${r.score},${r.level}`).join('\n');
        res.send(csv);
    } catch (err) {
        res.status(500).send("Error");
    }
});

// POST New Score
app.post('/api/scores', async (req, res) => {
    try {
        const { name, score, level } = req.body;
        if (!name || score === undefined || level === undefined) return res.status(400).send("Invalid data");
        await pool.query('INSERT INTO highscores (name, score, level) VALUES (?, ?, ?)', [name, score, level]);
        console.log(`[+] New score saved: ${name} - ${score} (Level ${level})`);
        res.send("Success");
    } catch (err) {
        res.status(500).send("Error");
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));

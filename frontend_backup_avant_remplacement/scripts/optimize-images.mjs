import sharp from 'sharp'
import { readFileSync, writeFileSync, readdirSync, statSync, existsSync } from 'fs'
import { join, extname, dirname, basename, relative, resolve } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const ROOT = resolve(__dirname, '..')
const IMAGES_DIR = join(ROOT, 'src', 'assets', 'images')
const TARGET_SIZE = 100 * 1024
const FORMATS = ['png', 'jpg', 'jpeg']

const SUPPORTED = new Set(FORMATS)

function walk(dir) {
  const files = []
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const full = join(dir, entry.name)
    if (entry.isDirectory()) {
      files.push(...walk(full))
    } else if (entry.isFile() && SUPPORTED.has(extname(entry.name).slice(1).toLowerCase())) {
      files.push(full)
    }
  }
  return files
}

async function optimize(inputPath) {
  const dir = dirname(inputPath)
  const base = basename(inputPath, extname(inputPath))
  const webpPath = join(dir, `${base}.webp`)

  const stats = statSync(inputPath)
  const inputSize = stats.size
  console.log(`\n  ${relative(IMAGES_DIR, inputPath)} (${(inputSize / 1024).toFixed(1)} KB)`)

  const pipeline = sharp(inputPath)
  const metadata = await pipeline.metadata()

  let quality = 85
  let bestQual = quality
  let bestSize = Infinity

  for (let attempt = 0; attempt < 8; attempt++) {
    const buffer = await pipeline
      .clone()
      .webp({ quality, effort: 6 })
      .toBuffer()

    if (buffer.length <= TARGET_SIZE) {
      writeFileSync(webpPath, buffer)
      console.log(`  -> ${base}.webp  (${(buffer.length / 1024).toFixed(1)} KB, q${quality})`)
      bestQual = quality
      bestSize = buffer.length
      break
    }

    if (buffer.length < bestSize) {
      bestSize = buffer.length
      bestQual = quality
    }

    quality -= 10
    if (quality < 20) quality = 20
  }

  if (bestSize > TARGET_SIZE) {
    const buffer = await pipeline.clone().webp({ quality: bestQual, effort: 6 }).toBuffer()
    writeFileSync(webpPath, buffer)
    console.log(`  -> ${base}.webp  (${(buffer.length / 1024).toFixed(1)} KB, q${bestQual})  ⚠️ > 100 KB`)
  }

  const pngBuffer = await pipeline
    .clone()
    .png({ compressionLevel: 9, palette: true })
    .toBuffer()

  if (pngBuffer.length < inputSize) {
    writeFileSync(inputPath, pngBuffer)
    console.log(`  -> ${base}.png  (${(pngBuffer.length / 1024).toFixed(1)} KB) optimisé`)
  } else {
    console.log(`  -> ${base}.png  déjà optimal (${(inputSize / 1024).toFixed(1)} KB)`)
  }

  return { inputSize, webpSize: bestSize, pngSize: Math.min(inputSize, pngBuffer.length) }
}

async function main() {
  if (!existsSync(IMAGES_DIR)) {
    console.log(`Le dossier ${IMAGES_DIR} n'existe pas.`)
    process.exit(1)
  }

  const files = walk(IMAGES_DIR)
  if (files.length === 0) {
    console.log('Aucune image trouvée dans src/assets/images/')
    return
  }

  console.log(`Optimisation de ${files.length} image(s)...\n`)

  let totalInput = 0
  let totalWebp = 0
  let totalPng = 0

  for (const file of files) {
    const result = await optimize(file)
    totalInput += result.inputSize
    totalWebp += result.webpSize
    totalPng += result.pngSize
  }

  console.log(`\n--- Bilan ---`)
  console.log(`PNG:    ${(totalPng / 1024).toFixed(1)} KB  (${(totalInput / 1024).toFixed(1)} KB avant)`)
  console.log(`WebP:   ${(totalWebp / 1024).toFixed(1)} KB`)
  console.log(`Total:  ${((totalPng + totalWebp) / 1024).toFixed(1)} KB`)
  console.log(`Économie: ${(((totalInput - totalPng) / totalInput) * 100).toFixed(1)}% sur PNG`)
}

main().catch(console.error)
